/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.features;

import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Attribute;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IFeatureExtractor;
import com.veridu.morpheus.interfaces.beans.IMongoDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.ICandidate;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.users.IFakeUsUser;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.LocalUtils;
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

@Component("firstNameExtractor")
@Scope("singleton")
public class BeanFirstNameFeatureExtractor implements IFeatureExtractor {

    private IDataSource dataSource;

    private IUtils utils;

    private IMongoDataSource mongo;

    private IFeatureExtractor facebookFeatureExtractor;

    private IFeatureExtractor linkedinFeatureExtractor;

    private IFeatureExtractor twitterFeatureExtractor;

    /**
     * Constructor
     *
     * @param dataSource injected idOS SQL data source
     * @param utils injected utils bean
     * @param mongo injected idOS NoSQL data source
     * @param facebookFeatureExtractor injected facebook feature extractor
     * @param linkedinFeatureExtractor injected linkedin feature extractor
     * @param twitterFeatureExtractor injected twitter feature extractor
     */
    @Autowired
    public BeanFirstNameFeatureExtractor(IDataSource dataSource, IUtils utils, IMongoDataSource mongo,
            @Qualifier("facebookExtractor") IFeatureExtractor facebookFeatureExtractor,
            @Qualifier("linkedinExtractor") IFeatureExtractor linkedinFeatureExtractor,
            @Qualifier("twitterExtractor") IFeatureExtractor twitterFeatureExtractor) {
        this.dataSource = dataSource;
        this.utils = utils;
        this.mongo = mongo;
        this.facebookFeatureExtractor = facebookFeatureExtractor;
        this.linkedinFeatureExtractor = linkedinFeatureExtractor;
        this.twitterFeatureExtractor = twitterFeatureExtractor;
    }

    private Instances facebookFeaturesHeader;
    private Instances linkedinFeaturesHeader;
    private Instances twitterFeaturesHeader;

    // save a cache of the facts list so we don't have to dynamically generate the rules
    // every time.
    private ArrayList<IFact> factList;

    private static final String myProviderName = "first-name-features";

    private static final String firstNameFactName = "firstName";

    private static final IFact amaFirstNameFact = new Fact(firstNameFactName, Constants.AMAZON_PROVIDER_NAME);
    private static final IFact twiFirstNameFact = new Fact(firstNameFactName, Constants.TWITTER_PROVIDER_NAME);
    private static final IFact payFirstNameFact = new Fact(firstNameFactName, Constants.PAYPAL_PROVIDER_NAME);
    private static final IFact linFirstNameFact = new Fact(firstNameFactName, Constants.LINKEDIN_PROVIDER_NAME);
    private static final IFact gooFirstNameFact = new Fact(firstNameFactName, Constants.GOOGLE_PROVIDER_NAME);
    private static final IFact fbkFirstNameFact = new Fact(firstNameFactName, Constants.FACEBOOK_PROVIDER_NAME);

    private int ruleIndexStart = -1;
    private int ruleIndexStop = -1; // last index exclusive

    // we store the following so we can execute the rules faster than doing a lot of string parsing
    private ArrayList<Integer> ruleOperand1 = new ArrayList<>(); // index of the first provider of a rule
    private ArrayList<Integer> ruleOperand2 = new ArrayList<>(); // index of the second provider of a rule
    private ArrayList<String> opCodes = new ArrayList<>(); // operator of the rule, e.g., =, <, >, <=, >=

    private static final String[] providers = { Constants.AMAZON_PROVIDER_NAME, Constants.TWITTER_PROVIDER_NAME,
            Constants.PAYPAL_PROVIDER_NAME, Constants.LINKEDIN_PROVIDER_NAME, Constants.GOOGLE_PROVIDER_NAME,
            Constants.FACEBOOK_PROVIDER_NAME };

    private static final double LEVENSHTEIN_SIMILARITY_THRESHOLD = 0.6;

    private static final boolean DEBUG = false;

    /**
     * called after bean construction
     */
    @PostConstruct
    private void init() {
        this.factList = new ArrayList<>();

        ruleIndexStart = 0;
        ruleIndexStop = ruleIndexStart;

        // now dynamically create rules that match birth year on all combinations of our providers
        for (int i = 0; i < (providers.length - 1); i++)
            for (int j = i + 1; j < providers.length; j++) {
                IFact fct = new Fact("is" + providers[i] + "FirstNameMatches" + providers[j] + "FirstName",
                        myProviderName);
                factList.add(fct);
                ruleOperand1.add(i);
                ruleOperand2.add(j);
                opCodes.add("=");
                ruleIndexStop++;
            }

        // add mongo facts
        factList.add(new Fact("isFacebookFirstNameMatchEmail", myProviderName));

        ArrayList<IFact> fbkFacts = this.facebookFeatureExtractor.obtainFactList();
        ArrayList<IFact> linkedinFacts = this.linkedinFeatureExtractor.obtainFactList();
        ArrayList<IFact> twitterFacts = this.twitterFeatureExtractor.obtainFactList();

        // add provider facts
        factList.addAll(fbkFacts);
        factList.addAll(linkedinFacts);
        factList.addAll(twitterFacts);

        this.facebookFeaturesHeader = this.utils.generateDatasetHeader(fbkFacts);
        this.linkedinFeaturesHeader = this.utils.generateDatasetHeader(linkedinFacts);
        this.twitterFeaturesHeader = this.utils.generateDatasetHeader(twitterFacts);
    }

    /**
     * Create an instance with first name features
     *
     * @param factory idOS API factory
     * @param dataset data header
     * @param user selected user
     * @return an Instance object with the features regarding first name
     */
    @Override
    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, IUser user) {
        // System.err.println("======> going to extract features for user " + user.getId());

        HashMap<IFact, String> firstNameFacts = dataSource.obtainSpecificFactForUser(factory, user, firstNameFactName);

        String[] firstNameList = { "-1", "-1", "-1", "-1", "-1", "-1" };

        firstNameList[0] = firstNameFacts.containsKey(amaFirstNameFact) ? firstNameFacts.get(amaFirstNameFact) : null;
        // System.err.println("amazon name => " + firstNameList[0]);
        firstNameList[1] = firstNameFacts.containsKey(twiFirstNameFact) ? firstNameFacts.get(twiFirstNameFact) : null;
        // System.err.println("twitter name => " + firstNameList[1]);
        // System.err.println("paypal name fact => " + firstNameFacts.get(payFirstNameFact));
        firstNameList[2] = firstNameFacts.containsKey(payFirstNameFact) ? firstNameFacts.get(payFirstNameFact) : null;
        // System.err.println("paypal name => " + firstNameList[2]);
        firstNameList[3] = firstNameFacts.containsKey(linFirstNameFact) ? firstNameFacts.get(linFirstNameFact) : null;
        // System.err.println("linkedin name => " + firstNameList[3]);
        firstNameList[4] = firstNameFacts.containsKey(gooFirstNameFact) ? firstNameFacts.get(gooFirstNameFact) : null;
        // System.err.println("google name => " + firstNameList[4]);
        firstNameList[5] = firstNameFacts.containsKey(fbkFirstNameFact) ? firstNameFacts.get(fbkFirstNameFact) : null;
        // System.err.println("facebook name => " + firstNameList[5]);

        Instance inst = new DenseInstance(dataset.numAttributes());
        inst.setDataset(dataset);

        int attPosCounter = 0;

        // apply the matching rules we've dynamically created
        for (int i = ruleIndexStart, j = 0; i < ruleIndexStop; i++, j++) {
            int op1 = ruleOperand1.get(j);
            int op2 = ruleOperand2.get(j);
            String opCode = opCodes.get(j);
            if ((firstNameList[op1] != null) && (firstNameList[op2] != null)) // otherwise it's just missing data
                LocalUtils.applyNameSimRule(inst, i, opCode, firstNameList[op1], firstNameList[op2],
                        LEVENSHTEIN_SIMILARITY_THRESHOLD);
            attPosCounter++;
        }

        // mongo features:
        Boolean fbkMatchEmailFirstName = null;

        JsonObject facebookProfile = this.mongo.getFacebookProfile(factory, user);

        if (facebookProfile != null)
            fbkMatchEmailFirstName = this.mongo.doesFacebookFirstNameMatchEmail(factory, user);

        LocalUtils.setBinaryValueAtPos(inst, attPosCounter++, fbkMatchEmailFirstName);

        // facebook feature extraction
        Instance fbkInst = facebookFeatureExtractor.createInstance(factory, this.facebookFeaturesHeader, user);
        for (int i = 0; i < (fbkInst.numAttributes() - 2); i++)
            inst.setValue(attPosCounter++, fbkInst.value(i));

        // linkedin feature extraction
        Instance linkedinInst = linkedinFeatureExtractor.createInstance(factory, this.linkedinFeaturesHeader, user);
        for (int i = 0; i < (linkedinInst.numAttributes() - 2); i++)
            inst.setValue(attPosCounter++, linkedinInst.value(i));

        // twitter feature extraction
        Instance twitterInst = twitterFeatureExtractor.createInstance(factory, this.twitterFeaturesHeader, user);
        for (int i = 0; i < (twitterInst.numAttributes() - 2); i++)
            inst.setValue(attPosCounter++, twitterInst.value(i));

        // apply the all missing rule from mlmodels:
        boolean allMissing = true;
        for (int i = 0; i < inst.numAttributes(); i++)
            if (!inst.isMissing(i)) {
                allMissing = false;
                break;
            }

        LocalUtils.setBinaryValueAtPos(inst, inst.numAttributes() - 2, allMissing);

        // figure out what is the supervision if we're creating instances for training
        if (user instanceof IFakeUsUser) {
            IFakeUsUser fuser = (IFakeUsUser) user;
            ArrayList<ICandidate> candidates = fuser.getAttributesMap().get(new Attribute(firstNameFactName));

            // TODO: later we should build this based on the candidates. for now we'll assume all candidates are
            // either real or fake.
            boolean isReal = false;
            if (candidates == null)
                isReal = candidates.get(0).isReal();
            String sup = isReal ? "real" : "fake";
            inst.setClassValue(sup);
        } else
            inst.setClassMissing();

        if (DEBUG) {
            System.out.println("--------------------------");
            System.out.println("First name feature extractor for user " + user.getId());
            System.out.println("Providers array:");
            System.out.println(Arrays.toString(providers));
            System.out.println("First name list (follows order of providers):");
            System.out.println(Arrays.toString(firstNameList));
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

