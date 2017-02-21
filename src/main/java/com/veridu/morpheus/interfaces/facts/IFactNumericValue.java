/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.facts;

/**
 * Wrapper for a numeric value
 */
public interface IFactNumericValue extends IFactValue {

    /**
     * Obtain the numeric value
     * @return the value
     */
    double getNumericFactValue();

}
