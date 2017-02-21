/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.json.JSONObject;

/**
 * This class wraps rules results, indicating which tests in a rule have passed and which
 * have failed.
 */
public class RuleResults {

    private JsonArray passed = new JsonArray();
    private JsonArray failed = new JsonArray();
    private JsonParser parser = new JsonParser();

    /**
     * Append a test that passed
     *
     * @param test encoded json test
     */
    public void appendPassedTest(JSONObject test) {
        this.passed.add(parser.parse(test.toString()));
    }

    /**
     * Append a test that failed
     *
     * @param test encoded json test
     */
    public void appendFailedTest(JSONObject test) {
        this.failed.add((parser.parse(test.toString())));
    }

    public JsonArray getPassed() {
        return passed;
    }

    public JsonArray getFailed() {
        return failed;
    }
}
