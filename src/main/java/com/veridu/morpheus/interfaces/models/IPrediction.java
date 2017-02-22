/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.models;

/**
 * Interface for a prediction object
 */
public interface IPrediction {

    /**
     * Gets the probability a user is real. When binary classifiers are used they can just set this to 1/0.
     *
     * @return the probability of being a real user
     */
    public double realUserProbability();

    /**
     * Gets the probability a user is fake. When binary classifiers are used they can just set this to 1/0.
     *
     * @return probability of being a fake user
     */
    public double fakeUserProbability();

    /**
     * Make a prediction based on a minimum threshold. For a user to be considered real (true) he/she has to have a real
     * user probability geq threshold. If he/she fails, false is returned to indicate a fake user.
     *
     * @param threshold the threshold for making a prediction
     * @return the thresholded prediction - a yes or no answer.
     */
    public boolean thresholdedPrediction(double threshold);

}
