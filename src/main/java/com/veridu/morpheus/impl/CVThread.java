package com.veridu.morpheus.impl;

import java.util.concurrent.Callable;

import weka.classifiers.Classifier;
import weka.core.Instances;

public class CVThread implements Callable<CVEvalReturn> {

    private Instances train;
    private Instances test;
    private Classifier classifier;
    private double threshold = -1;

    public CVThread(Classifier classifier, Instances train, Instances test, double threshold) {
        this.classifier = classifier;
        this.train = train;
        this.test = test;
        this.threshold = threshold;
    }

    public CVThread(Classifier classifier, Instances train, Instances test) {
        this.classifier = classifier;
        this.train = train;
        this.test = test;
    }

    @Override
    public CVEvalReturn call() throws Exception {

        // build classifier on training data
        this.classifier.buildClassifier(this.train);

        double[] pred = new double[this.test.numInstances()];
        double[] sup = new double[this.test.numInstances()];

        // make predictions on test set
        for (int i = 0; i < this.test.numInstances(); i++)
            if (this.threshold > 0) { // make a manual threshold based prediction
                double[] dist = this.classifier.distributionForInstance(this.test.get(i));
                if (dist[1] >= threshold) // probability of being real
                    pred[i] = 1;
                else
                    pred[i] = 0;
            } else // make a binary prediction
                pred[i] = this.classifier.classifyInstance(this.test.get(i));

        // save supervision for test set
        for (int i = 0; i < this.test.numInstances(); i++)
            sup[i] = this.test.get(i).classValue();

        return new CVEvalReturn(pred, sup);
    }

}
