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

    public User(String id) {
        this.id = id;
    }

    public User(String id, ArrayList<IProfile> profiles) {
        this.id = id;
        this.profiles = profiles;
    }

    @Override
    public HashMap<String, String> getCredentials() {
        return this.credentials;
    }

    @Override
    public void setCredentials(HashMap<String, String> credentials) {
        this.credentials = credentials;
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
    public void setProfiles(ArrayList<IProfile> profiles) {
        this.profiles = profiles;
    }

}
