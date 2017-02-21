/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.facts.IStringFactValue;

/**
 * Wrapper for a string fact
 */
public class StringFactValue implements IStringFactValue {

    private String value;

    public StringFactValue(String value) {
        this.value = value;
    }

    @Override
    public String getStringFactValue() {
        return this.value;
    }

}
