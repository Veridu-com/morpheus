/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.facts;

/**
 * Wrapper for a string fact value
 */
public interface IStringFactValue extends IFactValue {

    /**
     * The string fact value
     * @return the value
     */
    String getStringFactValue();

}
