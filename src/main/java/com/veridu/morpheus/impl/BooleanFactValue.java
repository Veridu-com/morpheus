package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.facts.IBooleanFactValue;

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
