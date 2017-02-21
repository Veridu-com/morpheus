/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.facts.ICandidate;

/**
 * A candidate for an attribute
 */
public class Candidate implements ICandidate {

    private String value;
    private boolean real;
    private double supportScore = 0;

    /**
     * Constructor
     * @param value candidate string value
     * @param real whether this candidate is real or not
     */
    public Candidate(String value, boolean real) {
        super();
        this.value = value;
        this.real = real;
    }

    /**
     * Constructor
     *
     * @param value candidate value
     * @param supportScore support score in [0,1] for this candidate
     */
    public Candidate(String value, double supportScore) {
        super();
        this.value = value;
        this.supportScore = supportScore;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean isReal() {
        return real;
    }

    public void setReal(boolean real) {
        this.real = real;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Candidate))
            return false;
        Candidate other = (Candidate) obj;
        return this.value.equals(other.getValue());
    }

    @Override
    public String toString() {
        return String.format("Candidate value: %s true/false: %b support score: %.2f", this.value, this.real,
                this.supportScore);
    }

    @Override
    public double getSupportScore() {
        return supportScore;
    }

    public void setSupportScore(double supportScore) {
        this.supportScore = supportScore;
    }

}
