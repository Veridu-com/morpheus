package com.veridu.skynet.models.birthday;

import com.veridu.morpheus.impl.GenericModel;
import com.veridu.morpheus.impl.Prediction;
import com.veridu.morpheus.interfaces.models.IPrediction;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;

import java.io.Serializable;

public class BirthDayMLPModel extends GenericModel implements Serializable {

    private static final long serialVersionUID = -6113599658194263979L;

    private MultilayerPerceptron mlpClassifier;

    public BirthDayMLPModel() {
    }

    public BirthDayMLPModel(MultilayerPerceptron mlpClassifier) {
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
