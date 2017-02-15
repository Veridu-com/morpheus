/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.interfaces.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public interface IUser extends Serializable {

    /**
     * Even though the user id is an integer, because it's used as profile_id in facts (which is type text), we
     * generalize to string to be sure that it can always hold an Id.
     *
     * @return
     */
    public String getId();

    /**
     * Returns the list of profiles a user has.
     *
     * @return
     */
    public ArrayList<IProfile> getProfiles();

    /**
     * Set the list of profiles.
     *
     * @param profiles
     */
    public void setProfiles(ArrayList<IProfile> profiles);

    public HashMap<String, String> getCredentials();

    public void setCredentials(HashMap<String, String> credentials);

}
