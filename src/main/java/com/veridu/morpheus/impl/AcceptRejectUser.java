/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.users.IAcceptRejectUser;
import com.veridu.morpheus.interfaces.users.IProfile;

import java.util.ArrayList;

/**
 * Represents an user of the old accept reject table
 */
public class AcceptRejectUser extends User implements IAcceptRejectUser {

    private boolean isReal;

    /**
     * Constructor
     * @param id user id
     */
    public AcceptRejectUser(String id) {
        super(id);
    }

    /**
     * Constructor
     *
     * @param id user id
     * @param isReal true if the user is not fake
     */
    public AcceptRejectUser(String id, boolean isReal) {
        super(id);
        this.isReal = isReal;
    }

    /**
     * Constructor
     * @param id user id
     * @param profiles list of profiles
     * @param isReal true if the user is not fake
     */
    public AcceptRejectUser(String id, ArrayList<IProfile> profiles, boolean isReal) {
        super(id, profiles);
        this.isReal = isReal;
    }

    @Override
    public boolean isReal() {
        return isReal;
    }

}
