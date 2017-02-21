/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.interfaces.users;

import java.io.Serializable;
import java.util.ArrayList;

public interface IUser extends Serializable {

    /**
     * Even though the user id is an integer, because it's used as profile_id in facts (which is type text), we
     * generalize to string to be sure that it can always hold an Id.
     *
     * @return the user id
     */
    String getId();

    /**
     * Returns the list of profiles a user has.
     *
     * @return the list of profiles
     */
    ArrayList<IProfile> getProfiles();

}
