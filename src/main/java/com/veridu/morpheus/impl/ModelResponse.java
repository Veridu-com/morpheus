package com.veridu.morpheus.impl;

/**
 * Created by cassio on 10/2/16.
 */
public class ModelResponse {

    private boolean response;

    public ModelResponse(boolean response) {
        this.response = response;
    }

    public boolean isResponse() {
        return response;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }
}
