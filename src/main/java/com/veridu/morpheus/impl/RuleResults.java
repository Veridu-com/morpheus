package com.veridu.morpheus.impl;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by cassio on 12/7/16.
 */
public class RuleResults {

    private ArrayList<JSONObject> passed = new ArrayList<>();
    private ArrayList<JSONObject> failed = new ArrayList<>();

    public void appendPassedTest(JSONObject test) {
        this.passed.add(test);
    }

    public void appendFailedTest(JSONObject test) {
        this.failed.add(test);
    }

}
