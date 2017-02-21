/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.tasks.flags;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.Parameters;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;

/**
 * Created by cassio on 10/4/16.
 */

@Component("flags-new")
public class NewProfileFlagTask implements ITask {

    private IUtils utils;

    private IDataSource dao;

    /**
     * Constructor
     * @param utils injected utils bean
     * @param dao injected idOS SQL data source
     */
    @Autowired
    public NewProfileFlagTask(IUtils utils, IDataSource dao) {
        this.utils = utils;
        this.dao = dao;
    }

    private static final Logger log = Logger.getLogger(NewProfileFlagTask.class);

    private static final String myProvider = "skynet";

    private static final String prefixFactName = "flagNewProfile";

    // *********************************************************************
    // List of new profile flags
    // *********************************************************************
    private static final String FLAG_NEW_ACCOUNT = "accountNew";
    private static final String FLAG_NEW_FACEBOOK = "facebookNew";
    private static final String FLAG_NEW_GOOGLE = "googleNew";
    private static final String FLAG_NEW_PAYPAL = "paypalNew";
    private static final String FLAG_NEW_TWITTER = "twitterNew";
    private static final String FLAG_NEW_YAHOO = "yahooNew";

    // *********************************************************************
    // List of provider facts
    // *********************************************************************

    private static final String profileAgeFactName = "profileAge";
    private static final IFact facebookProfileAge = new Fact(profileAgeFactName, Constants.FACEBOOK_PROVIDER_NAME);
    private static final IFact googleProfileAge = new Fact(profileAgeFactName, Constants.GOOGLE_PROVIDER_NAME);
    private static final IFact paypalProfileAge = new Fact(profileAgeFactName, Constants.PAYPAL_PROVIDER_NAME);
    private static final IFact twitterProfileAge = new Fact(profileAgeFactName, Constants.TWITTER_PROVIDER_NAME);
    private static final IFact yahooProfileAge = new Fact(profileAgeFactName, Constants.YAHOO_PROVIDER_NAME);

    // ****************************************************************************************
    // List of provider thresholds - considers a 99% percentile. Needs to be regularly updated
    // ****************************************************************************************
    // use http://www.unixtimestamp.com/index.php for conversions ;-)

    private static final int facebookThreshold = 1456921957; // 03/02/2016 @ 12:32pm (UTC)
    private static final int googleThreshold = 1453392750; // 01/21/2016 @ 4:12pm (UTC)
    private static final int paypalThreshold = 1468015200; // 07/08/2016 @ 10:00pm (UTC)
    private static final int twitterThreshold = 1470401890; // 08/05/2016 @ 12:58pm (UTC)
    private static final int yahooThreshold = 1472438086; // 08/29/2016 @ 2:34am (UTC)

    /**
     * Test if a profile is new
     * @param factory idOS API factory
     * @param user selected user
     * @param ageFact age fact to search for
     * @param provider provider name
     * @param threshold profile age to threshold on to determine if a profile is new
     * @param warningName name of the warning
     * @return true if the profile is new
     */
    private boolean testIfProfileIsNew(IdOSAPIFactory factory, IUser user, IFact ageFact, String provider,
            int threshold, String warningName) {
        boolean newProfile = false;

        HashMap<IFact, String> providerFacts = this.dao.obtainProviderFactsForUser(factory, user, provider);

        if (providerFacts.size() > 0)
            if (providerFacts.containsKey(ageFact) && (providerFacts.get(ageFact) != null)) {
                int age = Integer.parseInt(providerFacts.get(ageFact));
                if (age > threshold)
                    newProfile = true;
            } else
                newProfile = true;

        if (newProfile)
            this.dao.insertFlag(factory, user, warningName, Constants.PROFILE_PROVIDER_NAME);

        return newProfile;
    }

    /**
     * Run a new profile flag task
     * @param params request parameters
     */
    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {
        long time1 = System.currentTimeMillis(); // START CLOCK

        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;

        IUser user = new User(userId);

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

        // delete all warnings first
        this.dao.deleteFlag(factory, user, FLAG_NEW_YAHOO);
        this.dao.deleteFlag(factory, user, FLAG_NEW_GOOGLE);
        this.dao.deleteFlag(factory, user, FLAG_NEW_TWITTER);
        this.dao.deleteFlag(factory, user, FLAG_NEW_FACEBOOK);
        this.dao.deleteFlag(factory, user, FLAG_NEW_PAYPAL);
        this.dao.deleteFlag(factory, user, FLAG_NEW_ACCOUNT);

        boolean facebookNew = testIfProfileIsNew(factory, user, facebookProfileAge, Constants.FACEBOOK_PROVIDER_NAME,
                facebookThreshold, FLAG_NEW_FACEBOOK);

        boolean googleNew = testIfProfileIsNew(factory, user, googleProfileAge, Constants.GOOGLE_PROVIDER_NAME,
                googleThreshold, FLAG_NEW_GOOGLE);

        boolean paypalNew = testIfProfileIsNew(factory, user, paypalProfileAge, Constants.PAYPAL_PROVIDER_NAME,
                paypalThreshold, FLAG_NEW_PAYPAL);

        boolean twitterNew = testIfProfileIsNew(factory, user, twitterProfileAge, Constants.TWITTER_PROVIDER_NAME,
                twitterThreshold, FLAG_NEW_TWITTER);

        boolean yahooNew = testIfProfileIsNew(factory, user, yahooProfileAge, Constants.YAHOO_PROVIDER_NAME,
                yahooThreshold, FLAG_NEW_YAHOO);

        // ************************************************************************************
        // End of provider facts processing
        // ************************************************************************************
        long time2, timediff = 0;

        time2 = System.currentTimeMillis(); // STOP CLOCK
        timediff = time2 - time1;
        String factValue = "0";

        if (facebookNew || googleNew || paypalNew || twitterNew || yahooNew) {
            this.dao.insertFlag(factory, user, FLAG_NEW_ACCOUNT, Constants.PROFILE_PROVIDER_NAME);
            factValue = "1";
        }

        if (verbose)
            log.info(String.format("New profile flag returned %s for user %s in %d ms", factValue, userId,
                    time2 - time1));

        JsonObject responseBuilder = new JsonObject();

        responseBuilder.addProperty(Constants.MODEL_NAME_RESPONSE_STR, Constants.NEW_PROFILE_FLAG);
        responseBuilder.addProperty(Constants.USER_ID_RESPONSE_STR, userId);
        responseBuilder.addProperty(Constants.FLAG_VALUE, factValue);
        responseBuilder.addProperty(Constants.TIME_TAKEN_RESPONSE_STR, timediff);

        JsonArray arBuilder = new JsonArray();

        if (facebookNew)
            arBuilder.add(Constants.FACEBOOK_PROVIDER_NAME);

        if (googleNew)
            arBuilder.add(Constants.GOOGLE_PROVIDER_NAME);

        if (paypalNew)
            arBuilder.add(Constants.PAYPAL_PROVIDER_NAME);

        if (twitterNew)
            arBuilder.add(Constants.TWITTER_PROVIDER_NAME);

        if (yahooNew)
            arBuilder.add(Constants.YAHOO_PROVIDER_NAME);

        responseBuilder.add("newProfilesList", arBuilder);

        if (verbose)
            System.out.println(responseBuilder.toString());

    }
}

