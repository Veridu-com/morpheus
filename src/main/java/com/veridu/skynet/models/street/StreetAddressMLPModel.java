/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.skynet.models.street;

import com.veridu.morpheus.impl.GenericModel;
import com.veridu.morpheus.impl.Prediction;
import com.veridu.morpheus.interfaces.models.IPrediction;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;

import java.io.Serializable;

public class StreetAddressMLPModel extends GenericModel implements Serializable {

    private static final long serialVersionUID = -8975415468751498718L;

    private MultilayerPerceptron mlpClassifier;

    public StreetAddressMLPModel() {
    }

    /**
     * Constructor
     *
     * @param mlpClassifier the base classifier for this model
     */
    public StreetAddressMLPModel(MultilayerPerceptron mlpClassifier) {
        this.mlpClassifier = mlpClassifier;
    }

    /**
     * Make a prediction
     * @param instance
     *            instance to be predicted.
     * @return an IPrediction object with the results
     * @throws Exception if an error occurs, such as failure to load the ML model
     */
    @Override
    public IPrediction predict(Instance instance) throws Exception {
        double[] dist = this.mlpClassifier.distributionForInstance(instance);
        return new Prediction(dist[0], dist[1]);
    }

    /**
     * Obtain the underlying abstract classifier for this model
     *
     * @return the base classifier
     */
    @Override
    public AbstractClassifier getClassifier() {
        return this.mlpClassifier;
    }

}
