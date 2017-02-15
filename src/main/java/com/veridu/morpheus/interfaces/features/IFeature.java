/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.features;

import java.io.Serializable;

public interface IFeature extends Serializable {

    public String getProvider();

    public String getFactName();

}
