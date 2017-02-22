/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.features;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IFeatureExtractor;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.users.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cassio on 10/3/16.
 */
@Component("paypalExtractor")
public class BeanPaypalFeatureExtractor implements IFeatureExtractor {

    private IDataSource dataSource;

    private IUtils utils;

    private ArrayList<IFact> binaryFacts;
    private ArrayList<IFact> facts;

    /**
     * Constructor
     * @param dataSource injected idOS SQL data source
     * @param utils injected utils bean
     */
    @Autowired
    public BeanPaypalFeatureExtractor(IDataSource dataSource, IUtils utils) {
        this.dataSource = dataSource;
        this.utils = utils;
    }

    /**
     * called after bean construction
     */
    @PostConstruct
    public void init() {
        this.binaryFacts = this.utils.getPaypalBinaryFacts();
        this.facts = new ArrayList<>();
        this.facts.addAll(this.binaryFacts);
        this.facts.add(new Fact("isAccountVerified", "paypal"));
    }

    @Override
    public ArrayList<IFact> obtainFactList() {
        return this.facts;
    }

    /**
     * Create an instance with paypal features
     *
     * @param factory idOS API factory
     * @param dataset data header
     * @param user selected user
     * @return an Instance object with the features regarding paypal
     */
    @Override
    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, IUser user) {

        Instance inst = new DenseInstance(dataset.numAttributes());

        HashMap<IFact, Double> binaryFactsMap = this.dataSource
                .obtainBinaryFactsForProfile(factory, user, Constants.PAYPAL_PROVIDER_NAME);

        int attPos = 0;

        for (IFact fact : this.binaryFacts) {
            if (binaryFactsMap.containsKey(fact)) {
                Double value = binaryFactsMap.get(fact);
                inst.setValue(attPos, value);
            }
            attPos++;
        }

        // get the verified account fact:
        Boolean isVerified = this.dataSource.obtainFactValueIsPaypalVerified(factory, user);

        if (isVerified != null)
            if (isVerified)
                inst.setValue(attPos, 1);
            else
                inst.setValue(attPos, 0);
        else
            inst.setValue(attPos, Utils.missingValue());

        attPos++;

        return inst;
    }

}
