/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.facts;

/**
 * Wrapper for a boolean fact value
 */
public interface IBooleanFactValue extends IFactValue {

    /**
     * Obtain the boolean value
     *
     * @return get the value
     */
    boolean getBoolFactValue();

}
