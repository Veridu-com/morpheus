/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.models.IPrediction;

/**
 * Prediction class, which can express a prediction as a binary
 * output or a class probability.
 */
public class Prediction implements IPrediction {

    private double class0Prob = 0; // fake user
    private double class1Prob = 0; // real user

    /**
     * Constructor - the parameter values should sum to 1.0
     *
     * @param class0Prob probability of being fake
     * @param class1Prob probability of bein real
     */
    public Prediction(double class0Prob, double class1Prob) {
        super();
        this.class0Prob = class0Prob;
        this.class1Prob = class1Prob;
    }

    /**
     * Predicted real user probability
     *
     * @return probability in [0,1]
     */
    @Override
    public double realUserProbability() {
        return this.class1Prob;
    }

    /**
     * Make a binary tresholded prediction
     *
     * @param threshold the threshold for making a prediction
     *
     * @return true if a real user, false if fake.
     */
    @Override
    public boolean thresholdedPrediction(double threshold) {
        if (class1Prob >= threshold)
            return true; // predict a real user
        return false;
    }

    /**
     * Predicted fake user probability
     *
     * @return probability in [0,1]
     */
    @Override
    public double fakeUserProbability() {
        return this.class0Prob;
    }

    @Override
    public String toString() {
        return String.format("My prediction is: fake => %.2f real => %.2f", class0Prob, class1Prob);
    }

}
