/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import weka.classifiers.Classifier;
import weka.core.Instances;

import java.util.concurrent.Callable;

/**
 * Cross validation thread for use with CVMultithreadedEvaluateModel
 */
public class CVThread implements Callable<CVEvalReturn> {

    private Instances train;
    private Instances test;
    private Classifier classifier;
    private double threshold = -1;

    /**
     * Constructor
     *
     * @param classifier base classifier to use
     * @param train training dataset fold
     * @param test test dataset fold
     * @param threshold threshold value on which to make binary predictions
     */
    public CVThread(Classifier classifier, Instances train, Instances test, double threshold) {
        this.classifier = classifier;
        this.train = train;
        this.test = test;
        this.threshold = threshold;
    }

    /**
     * Constructor
     *
     * @param classifier base classifier to use
     * @param train training dataset fold
     * @param test test dataset fold
     */
    public CVThread(Classifier classifier, Instances train, Instances test) {
        this.classifier = classifier;
        this.train = train;
        this.test = test;
    }

    /**
     * Call method - for running the thread
     *
     * @return An evaluation result object
     * @throws Exception in case there is a problem when making predictions
     */
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
