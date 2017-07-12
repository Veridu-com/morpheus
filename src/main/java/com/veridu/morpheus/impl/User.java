/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.users.IProfile;
import com.veridu.morpheus.interfaces.users.IUser;

import java.util.ArrayList;
import java.util.HashMap;

public class User implements IUser {

    private String id;
    private ArrayList<IProfile> profiles;
    private String token;
    private HashMap<String, String> credentials;

    /**
     * Constructor
     * @param id user id
     */
    public User(String id) {
        this.id = id;
    }

    /**
     * Constructor
     * @param id user id
     * @param profiles profile list
     */
    public User(String id, ArrayList<IProfile> profiles) {
        this.id = id;
        this.profiles = profiles;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public ArrayList<IProfile> getProfiles() {
        return this.profiles;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User)
            return ((User) obj).getId().equals(this.getId());
        return false;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }
}
