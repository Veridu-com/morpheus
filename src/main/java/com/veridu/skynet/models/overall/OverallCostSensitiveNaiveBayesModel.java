/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.skynet.models.overall;

import com.veridu.morpheus.impl.GenericModel;
import com.veridu.morpheus.interfaces.models.IPrediction;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.Instance;

import java.io.Serializable;

public class OverallCostSensitiveNaiveBayesModel extends GenericModel implements Serializable {

    private static final long serialVersionUID = 2924602405709340418L;

    private CostSensitiveClassifier costClassifier;

    public OverallCostSensitiveNaiveBayesModel() {
    }

    /**
     * Constructor
     *
     * @param costClassifier the base classifier for this model
     */
    public OverallCostSensitiveNaiveBayesModel(CostSensitiveClassifier costClassifier) {
        this.costClassifier = costClassifier;
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
        throw new UnsupportedOperationException("This is a binary classifier.");
    }

    /**
     * Obtain the underlying abstract classifier for this model
     *
     * @return the base classifier
     */
    @Override
    public CostSensitiveClassifier getClassifier() {
        return this.costClassifier;
    }

    /**
     * Make a binary prediction
     *
     * @param instance instance to make a binary prediction
     * @return 0 if fake, 1 if true
     * @throws Exception  in case an error occurs such as mismatching header for instance
     */
    @Override
    public double binaryPrediction(Instance instance) throws Exception {
        return this.costClassifier.classifyInstance(instance);
    }

}
