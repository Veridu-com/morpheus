package com.veridu.skynet.models.overall;

import java.io.Serializable;

import com.veridu.morpheus.impl.GenericModel;
import com.veridu.morpheus.impl.Prediction;
import com.veridu.morpheus.interfaces.models.IPrediction;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;

public class OverallMLPModel extends GenericModel implements Serializable {

    private static final long serialVersionUID = 537318025286121908L;

    private MultilayerPerceptron mlpClassifier;

    public OverallMLPModel() {
    }

    public OverallMLPModel(MultilayerPerceptron mlpClassifier) {
        this.mlpClassifier = mlpClassifier;
    }

    @Override
    public IPrediction predict(Instance instance) throws Exception {
        double[] dist = this.mlpClassifier.distributionForInstance(instance);
        return new Prediction(dist[0], dist[1]);
    }

    public MultilayerPerceptron getClassifier() {
        return this.mlpClassifier;
    }

}
