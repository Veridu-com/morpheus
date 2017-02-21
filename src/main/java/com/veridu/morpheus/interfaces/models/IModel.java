/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.models;

import weka.classifiers.AbstractClassifier;
import weka.core.Instance;

import java.io.Serializable;

/**
 * Always recall that a model HAS to Serializable. NO shady network connections or crazy pointers.
 *
 * @author cassio
 *
 */
public interface IModel extends Serializable {

    /**
     * Make a prediction for a user.
     *
     * @param instance
     *            instance to be predicted.
     * @return a prediction object
     * @throws Exception if there is an error applying the instance to the model
     */
    public IPrediction predict(Instance instance) throws Exception;

    /**
     * Load a model from disk. When we get to Java 8 this method should be made static.
     *
     * @param modelPath
     *            path to load model from
     * @return the loaded model
     */
    public IModel loadModel(String modelPath);

    /**
     * Write the model to disk.
     *
     * @param modelPath
     *            path to save the model
     * @return true if the write operation was successfull
     */
    public boolean writeModel(String modelPath);

    /**
     * Obtain the underlying classifier used for this model.
     *
     * @return the underlying classifier
     */
    public AbstractClassifier getClassifier();

}
