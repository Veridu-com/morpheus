package com.veridu.skynet.models.lastname;

import java.io.Serializable;

import com.veridu.morpheus.impl.GenericModel;
import com.veridu.morpheus.impl.Prediction;
import com.veridu.morpheus.interfaces.models.IPrediction;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;

public class LastNameMLPModel extends GenericModel implements Serializable {

    private MultilayerPerceptron mlpClassifier;

    public LastNameMLPModel() {
    }

    public LastNameMLPModel(MultilayerPerceptron mlpClassifier) {
        this.mlpClassifier = mlpClassifier;
    }

    @Override
    public IPrediction predict(Instance instance) throws Exception {
        double[] dist = this.mlpClassifier.distributionForInstance(instance);
        return new Prediction(dist[0], dist[1]);
    }

    @Override
    public AbstractClassifier getClassifier() {
        return this.mlpClassifier;
    }

}
