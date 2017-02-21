/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.test.unit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by cassio on 11/11/16.
 */
public class MainTest {

    /**
     * Load file content for test from filesystem
     * @param resourcePath resource path
     * @return a string with the file contents
     */
    public static String loadFileContent(String resourcePath) {
        String filePath = TestIdOSSQL.class.getResource(resourcePath).getPath();
        String fileString = null;

        try {
            fileString = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileString;
    }

    /**
     * Load a response as json from a resource file
     *
     * @param resourcePath path to the resource
     *
     * @return a json with the response
     */
    public static JsonObject loadJsonResponse(String resourcePath) {
        return new JsonParser().parse(loadFileContent(resourcePath)).getAsJsonObject();
    }

    /**
     * Load a response as json from a resource file
     *
     * @param resourcePath path to the resource
     *
     * @return a json with the response
     */
    public static JSONObject loadJSONResponse(String resourcePath) {
        return new JSONObject(loadFileContent(resourcePath));
    }

    /**
     * Checks whether the API response is ok
     *
     * @param response json response
     * @return true if ok
     */
    protected static boolean isResponseOk(JsonObject response) {
        return response.get("status").getAsBoolean();
    }

    /**
     * Get the data field from the response
     * @param response API response
     * @return data field as a json object
     */
    protected JsonObject getResponseData(JsonObject response) {
        return response.get("data").getAsJsonObject();
    }

}
