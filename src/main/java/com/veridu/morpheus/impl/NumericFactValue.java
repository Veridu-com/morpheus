package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.facts.IFactNumericValue;

public class NumericFactValue implements IFactNumericValue {

    private double value;

    public NumericFactValue(double value) {
        this.value = value;
    }

    @Override
    public double getNumericFactValue() {
        return this.value;
    }

}
