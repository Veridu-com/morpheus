/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.tasks.recommendations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.idos.exceptions.InvalidToken;
import com.veridu.idos.exceptions.SDKException;
import com.veridu.idos.utils.IdOSAuthType;
import com.veridu.morpheus.impl.Age;
import com.veridu.morpheus.impl.RuleResults;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.utils.LocalUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;

/**
 * Created by cassio on 12/6/16.
 */
@Component("recommendation")
public class RecommendationTask {

    private final IUtils utils;

    private static final Logger log = Logger.getLogger(RecommendationTask.class);

    @Autowired
    public RecommendationTask(IUtils utils) {
        this.utils = utils;
    }

    private enum ConfLevel {
        none, low, medium, high
    }

    @Async
    public void runTask(JSONObject request) {
        String userName = request.getString("username");
        String pubKey = request.getString("publickey");

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userName));

        try {
            factory.getAttribute().setAuthType(IdOSAuthType.HANDLER);
            factory.getScore().setAuthType(IdOSAuthType.HANDLER);
            factory.getFlag().setAuthType(IdOSAuthType.HANDLER);
            factory.getGate().setAuthType(IdOSAuthType.HANDLER);
            factory.getFeature().setAuthType(IdOSAuthType.HANDLER);
            factory.getRecommendation().setAuthType(IdOSAuthType.HANDLER);
            factory.getReference().setAuthType(IdOSAuthType.HANDLER);
        } catch (InvalidToken invalidToken) {
            log.error("Invalid token");
            invalidToken.printStackTrace();
            return;
        }

        JsonArray failedRules = new JsonArray();

        JsonArray passedRules = new JsonArray();

        // run all rules
        JSONArray rules = request.getJSONArray("rules");

        for (int i = 0; i < rules.length(); i++) {
            JSONObject rule = rules.getJSONObject(i);
            String connector = rule.has("connector") ? rule.getString("connector") : "and";
            String ruleTag = rule.getString("tag");

            RuleResults results = processRule(rules.getJSONObject(i), factory, userName);

            JsonObject responseRule = new JsonObject();
            responseRule.addProperty("tag", ruleTag);
            responseRule.addProperty("connector", connector);
            responseRule.add("passed", results.getPassed());
            responseRule.add("failed", results.getFailed());

            if (resolvePass(results, connector))
                passedRules.add(responseRule);
            else
                failedRules.add(responseRule);
        }

        String pass = failedRules.size() == 0 ? "true" : "false";

        try {
            factory.getRecommendation().upsert(userName, pass, passedRules, failedRules);
            log.info("Recommendation written for user " + userName + " pass => " + pass);
        } catch (SDKException | UnsupportedEncodingException e) {
            log.error("Problems computing recommendation for user " + userName);
            e.printStackTrace();
        }

    }

    private boolean resolvePass(RuleResults results, String connector) {
        if (connector.equals("and")) {
            if (results.getFailed().size() == 0)
                return true;
        } else if (results.getPassed().size() > 0)
            return true;
        return false;
    }

    private RuleResults processRule(JSONObject rule, IdOSAPIFactory factory, String userName) {

        RuleResults results = new RuleResults();

        // run all tests
        JSONArray tests = rule.getJSONArray("tests");
        String cmpValueString;
        double cmpValueDouble;
        int cmpValueInt;
        String valueType;
        String opcode;

        for (int i = 0; i < tests.length(); i++) {
            JSONObject test = tests.getJSONObject(i);
            String category = test.getString("category");
            JsonObject response;
            JsonObject data;

            try {
                switch (category) {
                case "gate":
                    String gateSlug = test.getString("slug");
                    // obtain gate:
                    response = factory.getGate().getOne(userName, gateSlug);
                    data = response.getAsJsonObject("data");

                    String testConfidence = test.getString("confidence_level"); // reference value
                    String gateConfidence = data.get("confidence_level").getAsString(); // actual value

                    if (resolveGateConfLevelComparison(testConfidence, gateConfidence))
                        results.appendPassedTest(test);
                    else
                        results.appendFailedTest(test);
                    break;
                case "flag":
                    String flagSlug = test.getString("slug");
                    boolean exists = test.getBoolean("exists");
                    // obtain flag:
                    boolean notFound = false;
                    try {
                        factory.getFlag().getOne(userName, flagSlug);
                    } catch (SDKException e) {
                        if (e.getCode() == 404) {
                            notFound = true;
                            if (!exists)
                                results.appendPassedTest(test);
                            else
                                results.appendFailedTest(test);
                        } else {
                            results.appendFailedTest(test);
                            e.printStackTrace();
                        }
                    }

                    if (!notFound) { // flag exists
                        if (exists)
                            results.appendPassedTest(test);
                        else
                            results.appendFailedTest(test);
                    }
                    break;
                case "score":
                    String scoreName = test.getString("name");
                    cmpValueDouble = test.getDouble("cmp_value");
                    opcode = test.getString("operator");

                    // get the score
                    response = factory.getScore().getOne(userName, scoreName);
                    data = response.getAsJsonObject("data");

                    double actualValue = data.get("value").getAsDouble();

                    if (resolveDoubleComparison(cmpValueDouble, actualValue, opcode))
                        results.appendPassedTest(test);
                    else
                        results.appendFailedTest(test);

                    break;
                case "attribute":
                    valueType = test.getString("value_type");
                    String attName = test.getString("name");
                    opcode = test.getString("operator");

                    String attVal_str;
                    double attVal_num;

                    response = factory.getAttribute().getOne(userName, attName);
                    data = response.get("data").getAsJsonObject();

                    if (valueType.equals("string")) {
                        cmpValueString = test.getString("cmp_value");
                        attVal_str = data.get("value").getAsString();
                        if (resolveStringComparison(cmpValueString, attVal_str, opcode))
                            results.appendPassedTest(test);
                        else
                            results.appendFailedTest(test);
                    } else { // number
                        cmpValueDouble = test.getDouble("cmp_value");
                        attVal_num = Double.parseDouble(data.get("value").getAsString());
                        if (resolveDoubleComparison(cmpValueDouble, attVal_num, opcode))
                            results.appendPassedTest(test);
                        else
                            results.appendFailedTest(test);
                    }
                    break;
                case "feature":
                    valueType = test.getString("value_type");
                    int featureId = test.getInt("featureid");
                    opcode = test.getString("operator");

                    String featureVal_str;
                    double featureVal_num;

                    response = factory.getFeature().getOne(userName, featureId);
                    data = response.get("data").getAsJsonObject();

                    if (valueType.equals("string")) {
                        cmpValueString = test.getString("cmp_value");
                        featureVal_str = data.get("value").getAsString();
                        if (resolveStringComparison(cmpValueString, featureVal_str, opcode))
                            results.appendPassedTest(test);
                        else
                            results.appendFailedTest(test);
                    } else { // number feature
                        cmpValueDouble = test.getDouble("cmp_value");
                        featureVal_num = data.get("value").getAsDouble();
                        if (resolveDoubleComparison(cmpValueDouble, featureVal_num, opcode))
                            results.appendPassedTest(test);
                        else
                            results.appendFailedTest(test);
                    }
                    break;
                case "reference":
                    String refName = test.getString("name");
                    cmpValueString = test.getString("cmp_value");
                    opcode = test.getString("operator");

                    response = factory.getReference().getOne(userName, refName);
                    data = response.get("data").getAsJsonObject();

                    String refValue = data.get("value").getAsString();

                    if (resolveStringComparison(cmpValueString, refValue, opcode))
                        results.appendPassedTest(test);
                    else
                        results.appendFailedTest(test);
                    break;
                case "macro":
                    valueType = test.getString("value_type");
                    String macroName = test.getString("name");
                    opcode = test.getString("operator");

                    String macroVal_str;
                    double macroVal_num;

                    switch (macroName) {
                    case "age":
                        cmpValueInt = test.getInt("cmp_value");
                        // obtain attribute values for age
                        try {
                            Age age = Age.obtainAge(factory, userName);
                            if (age.validate()) {
                                int numAge = age.obtainAge();
                                if (resolveIntegerComparison(cmpValueInt, numAge, opcode))
                                    results.appendPassedTest(test);
                                else
                                    results.appendFailedTest(test);
                            } else
                                results.appendFailedTest(test);
                        } catch (SDKException e) {
                            results.appendFailedTest(test);
                        }
                        break;
                    case "sources":

                        cmpValueInt = test.getInt("cmp_value");

                        // obtain number of sources the guy has used
                        try {
                            factory.getSource().setAuthType(IdOSAuthType.HANDLER);
                            response = factory.getSource().listAll(userName);
                            int numSources;

                            if (LocalUtils.okResponse(response)) {
                                if (response.get("data").isJsonObject())
                                    numSources = 1;
                                else {
                                    JsonArray ar = response.get("data").getAsJsonArray();
                                    HashSet<String> sourceNames = new HashSet<>();
                                    for (int j = 0; j < ar.size(); j++) {
                                        String sName = ar.get(j).getAsJsonObject().get("name").getAsString();
                                        sourceNames.add(sName);
                                    }
                                    numSources = sourceNames.size();
                                }

                                if (resolveIntegerComparison(cmpValueInt, numSources, opcode))
                                    results.appendPassedTest(test);
                                else
                                    results.appendFailedTest(test);
                            } else {
                                results.appendFailedTest(test);
                            }

                        } catch (SDKException e) {
                            results.appendFailedTest(test);
                        }
                        break;
                    default:
                        results.appendFailedTest(test);
                    }
                    break;
                }
            } catch (SDKException e) {
                System.out.println("API request error: " + e.getMessage());
                results.appendFailedTest(test);
                e.printStackTrace();
            }
        }

        return results;
    }

    private boolean resolveGateConfLevelComparison(String testConfidence, String gateConfidence) {
        ConfLevel testConf = ConfLevel.valueOf(testConfidence);
        ConfLevel gateConf = ConfLevel.valueOf(gateConfidence);
        return gateConf.compareTo(testConf) >= 0;
    }

    private boolean resolveDoubleComparison(double cmpValue, double actualValue, String opcode) {
        switch (opcode) {
        case "!=":
            return cmpValue != actualValue;
        case "==":
            return cmpValue == actualValue;
        case ">=":
            return actualValue >= cmpValue;
        case ">":
            return actualValue > cmpValue;
        case "<=":
            return actualValue <= cmpValue;
        case "<":
            return actualValue < cmpValue;
        }
        return false;
    }

    private boolean resolveIntegerComparison(int cmpValue, int actualValue, String opcode) {
        switch (opcode) {
        case "!=":
            return cmpValue != actualValue;
        case "==":
            return cmpValue == actualValue;
        case ">=":
            return actualValue >= cmpValue;
        case ">":
            return actualValue > cmpValue;
        case "<=":
            return actualValue <= cmpValue;
        case "<":
            return actualValue < cmpValue;
        }
        return false;
    }

    private boolean resolveStringComparison(String cmpValue, String actualValue, String opcode) {
        switch (opcode) {
        case "!=":
            return !cmpValue.equals(actualValue);
        case "==":
            return cmpValue.equals(actualValue);
        case ">":
            return actualValue.compareTo(cmpValue) > 0;
        case "<":
            return actualValue.compareTo(cmpValue) < 0;
        }
        return false;
    }
}
