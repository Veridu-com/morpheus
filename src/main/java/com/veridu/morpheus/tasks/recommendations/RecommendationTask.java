package com.veridu.morpheus.tasks.recommendations;

import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.idos.exceptions.InvalidToken;
import com.veridu.idos.exceptions.SDKException;
import com.veridu.idos.utils.IdOSAuthType;
import com.veridu.morpheus.impl.RuleResults;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Created by cassio on 12/6/16.
 */
@Component("recommendation")
public class RecommendationTask {

    private IUtils utils;
    private IDataSource dao;

    private static final Logger log = Logger.getLogger(RecommendationTask.class);

    private static final boolean DEBUG = false;

    @Autowired
    public RecommendationTask(IUtils utils, IDataSource dao) {
        this.utils = utils;
        this.dao = dao;
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
        } catch (InvalidToken invalidToken) {
            invalidToken.printStackTrace();
            return;
        }

        ArrayList<JSONObject> failedRules = new ArrayList<>();
        ArrayList<JSONObject> passedRules = new ArrayList<>();

        // run all rules
        JSONArray rules = request.getJSONArray("rules");

        for (int i = 0; i < rules.length(); i++) {
            JSONObject rule = rules.getJSONObject(i);
            String connector = rule.has("connector") ? rule.getString("connector") : "and";
            String ruleTag = rule.getString("tag");

            RuleResults results = processRule(rules.getJSONObject(i), factory, userName);
            boolean pass = resolvePass(results, connector);
        }

    }

    private boolean resolvePass(RuleResults results, String connector) {
        return false;
    }

    private RuleResults processRule(JSONObject rule, IdOSAPIFactory factory, String userName) {

        RuleResults results = new RuleResults();

        // run all tests
        JSONArray tests = rule.getJSONArray("tests");

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
                    double cmpValue = test.getDouble("cmp_value");
                    String opcode = test.getString("operator");

                    // get the score
                    response = factory.getScore().getOne(userName, scoreName);
                    data = response.getAsJsonObject("data");

                    double actualValue = data.get("value").getAsDouble();

                    if (resolveDoubleComparison(cmpValue, actualValue, opcode))
                        results.appendPassedTest(test);
                    else
                        results.appendFailedTest(test);

                    break;
                case "attribute":

                    //String attributeName = factory.getAttribute().listAll(userName, Filter.createFilter().addNameFilter())
                    break;
                case "feature":
                    break;
                case "reference":
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
}
