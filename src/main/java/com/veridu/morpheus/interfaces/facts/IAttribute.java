/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.facts;

import java.io.Serializable;

/**
 * Interface for a generic attribute
 */
public interface IAttribute extends Serializable {

    /**
     * Return the attribute name
     *
     * @return att name
     */
    String getName();

}
