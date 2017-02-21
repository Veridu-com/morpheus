/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.facts.IFact;

import java.io.Serializable;

/**
 * This class is provided for efficient retrieval of facts from hashmaps. Please take note that equality comparison is
 * done based on the fact name and fact provider ONLY. That is intended behavior, so an Instance can be created quickly
 * by filling it with facts from a HashMap.
 *
 */
public class Fact implements IFact, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4400670615528196003L;
    private String name;
    private String provider;

    /**
     * Constructor - name and provider uniquely identify a fact
     * @param name fact name
     * @param provider fact provider
     */
    public Fact(String name, String provider) {
        this.name = name;
        this.provider = provider;
    }

    @Override
    public String toString() {
        return String.format("Fact => %s:%s", this.provider, this.name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getProvider() {
        return this.provider;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + this.provider.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Fact))
            return false;
        Fact other = (Fact) obj;
        return this.provider.equals(other.provider) && this.name.equals(other.name);
    }

}
