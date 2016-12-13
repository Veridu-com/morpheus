package com.veridu.morpheus.tasks.recommendations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.idos.exceptions.InvalidToken;
import com.veridu.idos.exceptions.SDKException;
import com.veridu.idos.utils.IdOSAuthType;
import com.veridu.morpheus.impl.RuleResults;
import com.veridu.morpheus.interfaces.beans.IUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

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
        String cmpValue_str;
        double cmpValue_num;
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
                    if (data.has("pass")) {
                        boolean testValue = test.getBoolean("pass"); // reference value
                        boolean gateValue = data.get("pass").getAsBoolean();
                        if (testValue == gateValue)
                            results.appendPassedTest(test);
                        else
                            results.appendFailedTest(test);
                    } else
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
                    cmpValue_num = test.getDouble("cmp_value");
                    opcode = test.getString("operator");

                    // get the score
                    response = factory.getScore().getOne(userName, scoreName);
                    data = response.getAsJsonObject("data");

                    double actualValue = data.get("value").getAsDouble();

                    if (resolveDoubleComparison(cmpValue_num, actualValue, opcode))
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
                        cmpValue_str = test.getString("cmp_value");
                        attVal_str = data.get("value").getAsString();
                        if (resolveStringComparison(cmpValue_str, attVal_str, opcode))
                            results.appendPassedTest(test);
                        else
                            results.appendFailedTest(test);
                    } else { // number
                        cmpValue_num = test.getDouble("cmp_value");
                        attVal_num = Double.parseDouble(data.get("value").getAsString());
                        if (resolveDoubleComparison(cmpValue_num, attVal_num, opcode))
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
                        cmpValue_str = test.getString("cmp_value");
                        featureVal_str = data.get("value").getAsString();
                        if (resolveStringComparison(cmpValue_str, featureVal_str, opcode))
                            results.appendPassedTest(test);
                        else
                            results.appendFailedTest(test);
                    } else { // number feature
                        cmpValue_num = test.getDouble("cmp_value");
                        featureVal_num = data.get("value").getAsDouble();
                        if (resolveDoubleComparison(cmpValue_num, featureVal_num, opcode))
                            results.appendPassedTest(test);
                        else
                            results.appendFailedTest(test);
                    }
                    break;
                case "reference":
                    String refName = test.getString("name");
                    cmpValue_str = test.getString("cmp_value");
                    opcode = test.getString("operator");

                    response = factory.getReference().getOne(userName, refName);
                    data = response.get("data").getAsJsonObject();

                    String refValue = data.get("value").getAsString();

                    if (resolveStringComparison(cmpValue_str, refValue, opcode))
                        results.appendPassedTest(test);
                    else
                        results.appendFailedTest(test);
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
