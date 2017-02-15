/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.features;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Attribute;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IFeatureExtractor;
import com.veridu.morpheus.interfaces.beans.IMongoDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.ICandidate;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.users.IFakeUsUser;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.LocalUtils;
import com.veridu.morpheus.utils.RulesEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by cassio on 10/3/16.
 */

@Scope("singleton")
@Component("cityExtractor")
public class BeanCityFeatureExtractor implements IFeatureExtractor {

    private IDataSource dataSource;

    private IUtils utils;

    private IMongoDataSource mongo;

    private IFeatureExtractor facebookFeatureExtractor;

    private IFeatureExtractor linkedinFeatureExtractor;

    private IFeatureExtractor twitterFeatureExtractor;

    private IFeatureExtractor googleFeatureExtractor;

    private IFeatureExtractor paypalFeatureExtractor;

    private Instances facebookFeaturesHeader;
    private Instances linkedinFeaturesHeader;
    private Instances twitterFeaturesHeader;
    private Instances googleFeaturesHeader;
    private Instances paypalFeaturesHeader;

    private ArrayList<IFact> factList = new ArrayList<>();

    private static final String[] providersCityName = { Constants.CPR_PROVIDER_NAME, Constants.LINKEDIN_PROVIDER_NAME,
            Constants.PERSONAL_PROVIDER_NAME, Constants.PAYPAL_PROVIDER_NAME, Constants.YAHOO_PROVIDER_NAME };

    private static final String[] providersCurrentCity = { Constants.FACEBOOK_PROVIDER_NAME,
            Constants.GOOGLE_PROVIDER_NAME };

    private static String[] providers = new String[providersCityName.length + providersCurrentCity.length];

    private static final ArrayList<IFact> providerCityFacts = LocalUtils
            .generateProvidersFact(providersCityName, "cityName");

    private static final ArrayList<IFact> providerCurrentCityFacts = LocalUtils
            .generateProvidersFact(providersCurrentCity, "currentCityName");

    private static ArrayList<IFact> providerFacts;

    private static final String myProviderName = "city-features";

    private static RulesEngine rulesEngine;

    private static final boolean DEBUG = false;

    @Autowired
    public BeanCityFeatureExtractor(IDataSource dataSource, IUtils utils, IMongoDataSource mongo,
            @Qualifier("facebookExtractor") IFeatureExtractor facebookFeatureExtractor,
            @Qualifier("linkedinExtractor") IFeatureExtractor linkedinFeatureExtractor,
            @Qualifier("twitterExtractor") IFeatureExtractor twitterFeatureExtractor,
            @Qualifier("googleExtractor") IFeatureExtractor googleFeatureExtractor,
            @Qualifier("paypalExtractor") IFeatureExtractor paypalFeatureExtractor) {
        this.dataSource = dataSource;
        this.utils = utils;
        this.mongo = mongo;
        this.facebookFeatureExtractor = facebookFeatureExtractor;
        this.linkedinFeatureExtractor = linkedinFeatureExtractor;
        this.twitterFeatureExtractor = twitterFeatureExtractor;
        this.googleFeatureExtractor = googleFeatureExtractor;
        this.paypalFeatureExtractor = paypalFeatureExtractor;
    }

    @PostConstruct
    private void init() {
        int i = 0;
        for (; i < providersCityName.length; i++)
            providers[i] = providersCityName[i];
        for (int j = 0; j < providersCurrentCity.length; j++, i++)
            providers[i] = providersCurrentCity[j];

        providerFacts = new ArrayList<>();
        providerFacts.addAll(providerCityFacts);
        providerFacts.addAll(providerCurrentCityFacts);

        rulesEngine = new RulesEngine("cityName", myProviderName, providers, false, 0.6);

        // generic providers extractors
        this.factList.addAll(this.facebookFeatureExtractor.obtainFactList());
        this.factList.addAll(this.linkedinFeatureExtractor.obtainFactList());
        this.factList.addAll(this.twitterFeatureExtractor.obtainFactList());
        this.factList.addAll(this.googleFeatureExtractor.obtainFactList());
        this.factList.addAll(this.paypalFeatureExtractor.obtainFactList());

        this.factList.addAll(rulesEngine.getFactList());

        this.facebookFeaturesHeader = this.utils.generateDatasetHeader(this.facebookFeatureExtractor.obtainFactList());
        this.linkedinFeaturesHeader = this.utils.generateDatasetHeader(this.linkedinFeatureExtractor.obtainFactList());
        this.twitterFeaturesHeader = this.utils.generateDatasetHeader(this.twitterFeatureExtractor.obtainFactList());
        this.googleFeaturesHeader = this.utils.generateDatasetHeader(this.googleFeatureExtractor.obtainFactList());
        this.paypalFeaturesHeader = this.utils.generateDatasetHeader(this.paypalFeatureExtractor.obtainFactList());
    }

    @Override
    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, IUser user) {

        Instance inst = new DenseInstance(dataset.numAttributes());
        inst.setDataset(dataset);

        int attPosCounter = 0;

        // get facts from basic extractors
        Instance facebookInst = this.facebookFeatureExtractor
                .createInstance(factory, this.facebookFeaturesHeader, user);
        Instance linkedinInst = this.linkedinFeatureExtractor
                .createInstance(factory, this.linkedinFeaturesHeader, user);
        Instance twitterInst = this.twitterFeatureExtractor.createInstance(factory, this.twitterFeaturesHeader, user);
        Instance googleInst = this.googleFeatureExtractor.createInstance(factory, this.googleFeaturesHeader, user);
        Instance paypalInst = this.paypalFeatureExtractor.createInstance(factory, this.paypalFeaturesHeader, user);

        for (int i = 0; i < (facebookInst.numAttributes() - 2); i++)
            inst.setValue(attPosCounter++, facebookInst.value(i));

        for (int i = 0; i < (linkedinInst.numAttributes() - 2); i++)
            inst.setValue(attPosCounter++, linkedinInst.value(i));

        for (int i = 0; i < (twitterInst.numAttributes() - 2); i++)
            inst.setValue(attPosCounter++, twitterInst.value(i));

        for (int i = 0; i < (googleInst.numAttributes() - 2); i++)
            inst.setValue(attPosCounter++, googleInst.value(i));

        for (int i = 0; i < (paypalInst.numAttributes() - 2); i++)
            inst.setValue(attPosCounter++, paypalInst.value(i));

        // get provider facts
        HashMap<IFact, String> userFacts = dataSource.obtainSpecificFactForUser(factory, user, "*city*");
        ArrayList<String> factValues = new ArrayList<>();

        for (IFact provFact : providerFacts)
            if (userFacts.containsKey(provFact))
                factValues.add(userFacts.get(provFact));
            else
                factValues.add("");

        // rules
        double[] values = rulesEngine.applyRules(factValues);
        for (int i = 0; i < values.length; i++)
            inst.setValue(attPosCounter++, values[i]);

        // apply the all missing rule from mlmodels:
        LocalUtils.setBinaryValueAtPos(inst, inst.numAttributes() - 2, LocalUtils.allAttributesMissing(inst));

        // figure out what is the supervision if we're creating instances for training
        if (user instanceof IFakeUsUser) {
            IFakeUsUser fuser = (IFakeUsUser) user;
            ArrayList<ICandidate> candidates = fuser.getAttributesMap().get(new Attribute("city"));

            // TODO: later we should build this based on the candidates. for now we'll assume all candidates are
            // either real or fake.
            boolean isReal = false;
            if (candidates != null)
                isReal = candidates.get(0).isReal();
            String sup = isReal ? "real" : "fake";
            inst.setClassValue(sup);
        } else
            inst.setClassMissing();

        if (DEBUG) {
            System.out.println("--------------------------");
            System.out.println("City feature extractor for user " + user.getId());
            System.out.println("Providers array:");
            System.out.println(Arrays.toString(providers));
            System.out.println("City list (follows order of providers):");
            System.out.println(Arrays.toString(factValues.toArray()));
            System.out.println("Resulting instance:");
            System.out.println(inst);
            System.out.println("--------------------------");
        }

        return inst;
    }

    @Override
    public ArrayList<IFact> obtainFactList() {
        return this.factList;
    }

}
