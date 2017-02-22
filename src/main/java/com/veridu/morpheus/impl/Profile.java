/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.users.IProfile;

/**
 * This class represents a user profile, which associates a provider
 * name to a profile ID related to that provider. A provider name
 * and a profile id uniquely identify a profile. Note that two
 * equal profile ids may not indicate the same profile, as they
 * can belong to different providers.
 */
public class Profile implements IProfile {

    private String provider;
    private String profileId;

    /**
     * Constructor
     *
     * @param provider provider name
     * @param profileId profile id as string
     */
    public Profile(String provider, String profileId) {
        super();
        this.provider = provider;
        this.profileId = profileId;
    }

    @Override
    public String getProvider() {
        return this.provider;
    }

    @Override
    public String getProfileId() {
        return this.profileId;
    }

    @Override
    public String toString() {
        return String.format("Profile => %s - %s", this.provider, this.profileId);
    }

    @Override
    public int hashCode() {
        return this.provider.hashCode() + this.profileId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Profile))
            return false;
        Profile other = (Profile) obj;
        return this.provider.equals(other.getProvider()) && this.profileId.equals(other.getProfileId());
    }

}
