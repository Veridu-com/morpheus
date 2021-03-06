/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.facts.IBooleanFactValue;

/**
 * A boolean fact value
 */
public class BooleanFactValue implements IBooleanFactValue {

    private boolean value;

    public BooleanFactValue(boolean value) {
        this.value = value;
    }

    @Override
    public boolean getBoolFactValue() {
        return this.value;
    }

}
