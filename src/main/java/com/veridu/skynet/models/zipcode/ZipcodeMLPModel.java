/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.skynet.models.zipcode;

import com.veridu.morpheus.impl.GenericModel;
import com.veridu.morpheus.impl.Prediction;
import com.veridu.morpheus.interfaces.models.IPrediction;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;

import java.io.Serializable;

public class ZipcodeMLPModel extends GenericModel implements Serializable {

    private static final long serialVersionUID = 8250520906488678628L;

    private MultilayerPerceptron mlpClassifier;

    public ZipcodeMLPModel() {
    }

    public ZipcodeMLPModel(MultilayerPerceptron mlpClassifier) {
        this.mlpClassifier = mlpClassifier;
    }

    @Override
    public IPrediction predict(Instance instance) throws Exception {
        double[] dist = this.mlpClassifier.distributionForInstance(instance);
        return new Prediction(dist[0], dist[1]);
    }

    @Override
    public MultilayerPerceptron getClassifier() {
        return this.mlpClassifier;
    }

}
