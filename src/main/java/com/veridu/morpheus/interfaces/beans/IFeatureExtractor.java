/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.beans;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.users.IUser;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;

/**
 * Every feature extractor should implement this interface
 */
public interface IFeatureExtractor {

    /**
     * Create an instance
     *
     * @param factory idOS API factory
     * @param dataset dataset header
     * @param user selected user
     * @return an instance containing features values defined by this extractor
     */
    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, IUser user);

    /**
     * Obtain the facts list this extractor generates
     *
     * @return facts list
     */
    public ArrayList<IFact> obtainFactList();

}
