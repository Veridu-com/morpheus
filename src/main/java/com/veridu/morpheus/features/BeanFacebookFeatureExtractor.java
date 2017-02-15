/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.features;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IFeatureExtractor;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.LocalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cassio on 10/3/16.
 */

@Component("facebookExtractor")
@Scope("singleton")
public class BeanFacebookFeatureExtractor implements IFeatureExtractor {

    private IDataSource dataSource;

    private IUtils utils;

    private ArrayList<IFact> numericFacts;
    private ArrayList<IFact> facts;

    @Autowired
    public BeanFacebookFeatureExtractor(IDataSource dataSource, IUtils utils) {
        this.dataSource = dataSource;
        this.utils = utils;
    }

    @PostConstruct
    public void init() {
        this.numericFacts = this.utils.getFacebookNumericFacts();
        this.facts = new ArrayList<>();
        this.facts.addAll(this.numericFacts);
        this.facts.add(new Fact("isFacebookEmailDomainInTempDomainList", "facebook"));
    }

    @Override
    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, IUser user) {

        Instance inst = new DenseInstance(dataset.numAttributes());

        HashMap<IFact, Double> numericFactsMap = this.dataSource
                .obtainNumericFactsForProfile(factory, user, "facebook");

        int attPos = 0;

        for (IFact fact : this.numericFacts) {
            if (numericFactsMap.containsKey(fact)) {
                Double value = numericFactsMap.get(fact);
                inst.setValue(attPos, value);
            }
            attPos++;
        }

        // get the facebook email fact:
        String fbkEmail = this.dataSource.obtainFacebookEmail(factory, user);

        if (fbkEmail != null) {
            String unserializedEmail = fbkEmail;
            String domain = LocalUtils.extractDomain(unserializedEmail);
            boolean isTemp = this.utils.isDomainInTemporaryDomainsList(domain);
            // System.err.println(
            // "got facebook email domain => " + domain + " for user " + user.getId() + " isTemp = " + isTemp);
            if (isTemp)
                inst.setValue(attPos, 1);
            else
                inst.setValue(attPos, 0);
        } else
            inst.setValue(attPos, 0);
        attPos++;

        return inst;
    }

    @Override
    public ArrayList<IFact> obtainFactList() {
        return this.facts;
    }

}
