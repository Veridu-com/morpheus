package com.veridu.morpheus.interfaces.facts;

import java.io.Serializable;

public interface ICandidate extends Serializable {

    public String getValue();

    public boolean isReal();

    public double getSupportScore();

}
