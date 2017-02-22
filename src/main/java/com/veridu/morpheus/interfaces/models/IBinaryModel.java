/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.models;

import weka.core.Instance;

/**
 * Interface for a binary model
 */
public interface IBinaryModel extends IModel {

    /**
     * Make a prediction.
     * @param instance instance on which to make prediction
     * @return 1 if a real person; 0 if fake
     * @throws Exception if anything goes awry during prediction
     */
    public double binaryPrediction(Instance instance) throws Exception;

}
