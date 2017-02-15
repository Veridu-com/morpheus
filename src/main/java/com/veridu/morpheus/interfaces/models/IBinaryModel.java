/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.models;

import weka.core.Instance;

public interface IBinaryModel extends IModel {

    public double binaryPrediction(Instance instance) throws Exception;

}
