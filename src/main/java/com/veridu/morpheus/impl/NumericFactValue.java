/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

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
