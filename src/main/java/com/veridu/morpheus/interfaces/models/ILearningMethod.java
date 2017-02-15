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
     */
    public IModel train(Instances dataset, IModelParams params) throws Exception;

}
