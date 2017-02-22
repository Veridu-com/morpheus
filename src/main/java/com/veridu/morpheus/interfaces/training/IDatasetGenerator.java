/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.training;

import weka.core.Instances;

/**
 * Interface for dataset generation
 */
public interface IDatasetGenerator {

    /**
     * Create a dataset
     *
     * @return the instances object
     */
    Instances createDataset();

}
