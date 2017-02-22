/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.facts;

import java.io.Serializable;

/**
 * Interface defining an attribute candidate
 */
public interface ICandidate extends Serializable {

    /**
     * Get the string value of the candidate
     * @return string value
     */
    public String getValue();

    /**
     * Return whether this candidate is real or fake
     * @return true for real; false for fake
     */
    public boolean isReal();

    /**
     * Return the support score
     * @return value in [0,1]
     */
    public double getSupportScore();

}
