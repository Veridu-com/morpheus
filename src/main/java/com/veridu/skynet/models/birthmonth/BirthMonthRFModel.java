/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.skynet.models.birthmonth;

import com.veridu.morpheus.impl.GenericModel;
import com.veridu.morpheus.impl.Prediction;
import com.veridu.morpheus.interfaces.models.IPrediction;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;

import java.io.Serializable;

public class BirthMonthRFModel extends GenericModel implements Serializable {

    private static final long serialVersionUID = 5865597200862199228L;

    private RandomForest randomForest;

    public BirthMonthRFModel() {
    }

    public BirthMonthRFModel(RandomForest randomForest) {
        this.randomForest = randomForest;
    }

    @Override
    public IPrediction predict(Instance instance) throws Exception {
        double[] dist = this.randomForest.distributionForInstance(instance);
        return new Prediction(dist[0], dist[1]);
    }

    @Override
    public AbstractClassifier getClassifier() {
        return this.randomForest;
    }

}
