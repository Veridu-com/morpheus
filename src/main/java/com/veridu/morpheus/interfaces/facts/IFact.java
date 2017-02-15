/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.facts;

import java.io.Serializable;

public interface IFact extends Serializable {

    /**
     * gets the fact name, e.g., numOfFriends
     *
     * @return
     */
    public String getName();

    /**
     * gets the provider, e.g., facebook
     *
     * @return
     */
    public String getProvider();

}
