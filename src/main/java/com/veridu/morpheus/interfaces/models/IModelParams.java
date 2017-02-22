/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.models;

/**
 * Interface for model parameters
 */
public interface IModelParams {

    /**
     * Gets the string parameters as an array
     *
     * @return string parameters
     */
    public String[] getStringParams();

    /**
     * Gets the integer parameters
     *
     * @return integer parameters
     */
    public int[] getIntParams();

    /**
     * Gets the double parameters
     *
     * @return double parameters
     */
    public double[] getDoubleParams();

    /**
     * Gets the boolean parameters
     *
     * @return boolean parameters
     */
    public boolean[] getBooleanParams();

}
