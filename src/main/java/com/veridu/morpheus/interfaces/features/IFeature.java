/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.features;

import java.io.Serializable;

/**
 * Interface for a feature
 */
public interface IFeature extends Serializable {

    /**
     * Get the provider of this feature
     *
     * @return the provider
     */
    String getProvider();

    /**
     * Return the fact name
     *
     * @return the fact name
     */
    String getFactName();

}
