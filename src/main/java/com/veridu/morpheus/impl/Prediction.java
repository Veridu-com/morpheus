/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.models.IPrediction;

public class Prediction implements IPrediction {

    private double class0Prob = 0; // fake user
    private double class1Prob = 0; // real user

    public Prediction(double class0Prob, double class1Prob) {
        super();
        this.class0Prob = class0Prob;
        this.class1Prob = class1Prob;
    }

    @Override
    public double realUserProbability() {
        return this.class1Prob;
    }

    @Override
    public boolean thresholdedPrediction(double threshold) {
        if (class1Prob >= threshold)
            return true; // predict a real user
        return false;
    }

    @Override
    public double fakeUserProbability() {
        return this.class0Prob;
    }

    @Override
    public String toString() {
        return String.format("My prediction is: fake => %.2f real => %.2f", class0Prob, class1Prob);
    }

}
