/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.interfaces.users;

import java.io.Serializable;

public interface IProfile extends Serializable {

    /**
     * Get the provider
     *
     * @return provider name
     */
    public String getProvider();

    /**
     * Get a profile Id, which is a string.
     *
     * @return profile id
     */
    public String getProfileId();

}
