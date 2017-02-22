/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.models.IBinaryModel;
import com.veridu.morpheus.interfaces.models.IModel;

import weka.core.Instance;
import weka.core.SerializationHelper;

/**
 * New machine learning models should extend this class.
 *
 */
public abstract class GenericModel implements IBinaryModel {

    private static final long serialVersionUID = -887093987800014116L;

    /**
     * Load a model from a binary file
     *
     * @param modelPath
     *            path to load model from
     * @return loaded model
     */
    @Override
    public IModel loadModel(String modelPath) {
        try {
            return (IModel) SerializationHelper.read(modelPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Write a model to disk
     *
     * @param modelPath
     *            path to save the model
     * @return true if write was successful
     */
    @Override
    public boolean writeModel(String modelPath) {
        try {
            SerializationHelper.write(modelPath, this);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Perform a binary prediction
     *
     * @param instance instance to make a binary prediction
     * @return 0 if a fake profile, 1 if real
     * @throws Exception in case there is a problem with the predict method
     */
    @Override
    public double binaryPrediction(Instance instance) throws Exception {
        return -1;
    }

}
