/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.json.JSONObject;

/**
 * Created by cassio on 12/7/16.
 */
public class RuleResults {

    private JsonArray passed = new JsonArray();
    private JsonArray failed = new JsonArray();
    private JsonParser parser = new JsonParser();

    public void appendPassedTest(JSONObject test) {
        this.passed.add(parser.parse(test.toString()));
    }

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
