package com.veridu.skynet.models.overall;

import java.io.Serializable;

import com.veridu.morpheus.impl.GenericModel;
import com.veridu.morpheus.interfaces.models.IPrediction;

import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.Instance;

public class OverallCostSensitiveNaiveBayesModel extends GenericModel implements Serializable {

    private static final long serialVersionUID = 2924602405709340418L;

    private CostSensitiveClassifier costClassifier;

    public OverallCostSensitiveNaiveBayesModel() {
    }

    public OverallCostSensitiveNaiveBayesModel(CostSensitiveClassifier costClassifier) {
        this.costClassifier = costClassifier;
    }

    @Override
    public IPrediction predict(Instance instance) throws Exception {
        throw new UnsupportedOperationException("This is a binary classifier.");
    }

    @Override
    public CostSensitiveClassifier getClassifier() {
        return this.costClassifier;
    }

    @Override
    public double binaryPrediction(Instance instance) throws Exception {
        return this.costClassifier.classifyInstance(instance);
    }

}
