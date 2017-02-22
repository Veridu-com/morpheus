/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.models;

import weka.core.Instances;

public interface ILearningMethod {

    /**
     * Train a learning method using a dataset.
     *
     * @param dataset
     *            training data
     * @param params
     *            model parameters
     * @return a serializable model
     * @throws Exception in case there is a problem during training, such as wrong model parameters
     */
    public IModel train(Instances dataset, IModelParams params) throws Exception;

}
