package com.veridu.morpheus.impl;

/**
 * Created by cassio on 10/2/16.
 */
public class ModelResponse {

    private boolean status;
    private String reason = "";

    public ModelResponse(boolean status) {
        this.status = status;
    }

    public ModelResponse(boolean status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
