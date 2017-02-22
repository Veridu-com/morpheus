/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

/**
 * Created by cassio on 10/2/16.
 */

/**
 * This class wraps a model response
 */
public class ModelResponse {

    private boolean status;

    /**
     * Constructor
     * @param status true for success, false otherwise
     */
    public ModelResponse(boolean status) {
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

}
