/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

/**
 * Created by cassio on 10/2/16.
 */
public class ModelResponseFailure extends ModelResponse {

    private String reason;

    public ModelResponseFailure(boolean status, String reason) {
        super(status);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
