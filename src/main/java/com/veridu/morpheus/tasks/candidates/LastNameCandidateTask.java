/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.tasks.candidates;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IMongoDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.ICandidate;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.utils.LocalUtils;
import com.veridu.morpheus.utils.Parameters;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cassio on 10/4/16.
 */
@Component("lastname-candidates")
public class LastNameCandidateTask implements ITask {

    private IDataSource dataSource;

    private IUtils utils;

    private IMongoDataSource mongo;

    private static final String lastNameFactName = "lastName";

    Logger logger = Logger.getLogger(LastNameCandidateTask.class);

    /**
     * Constructor
     * @param dataSource injected idOS SQL data source
     * @param utils injected utils bean
     * @param mongo injected idOS NoSQL data source
     */
    @Autowired
    public LastNameCandidateTask(IDataSource dataSource, IUtils utils, IMongoDataSource mongo) {
        this.dataSource = dataSource;
        this.utils = utils;
        this.mongo = mongo;
    }

    /**
     * Search for str in cands and increase support in case found
     * @param cands candidates hashmap with string and support values
     * @param str the string to search for
     */
    private static void searchForStringInCandidates(HashMap<String, Double> cands, String str) {
        if (str != null)
            for (String cand : cands.keySet())
                if (str.contains(cand.toLowerCase()))
                    cands.put(cand, cands.get(cand) + 1.0);
    }

    /**
     * Obtain an array list of direct family members (mother,father,brothers,sisters) last names
     * @param fbkFamily facebook family members as a json array
     * @return the last names
     */
    private static ArrayList<String> obtainDirectRelativesLastNames(JsonArray fbkFamily) {
        ArrayList<String> lastNames = new ArrayList<>();

        if (fbkFamily != null) {
            fbkFamily.forEach(k -> {
                JsonObject jobj = k.getAsJsonObject();
                if (LocalUtils.validateJsonObject(jobj) && LocalUtils.validateJsonField(jobj, "relationship")) {
                    String relationShip = jobj.get("relationship").getAsString();
                    if (relationShip.equals("sister") || relationShip.equals("brother") || relationShip.equals("father")
                            || relationShip.equals("mother"))
                        if (LocalUtils.validateJsonField(jobj, "last_name"))
                            lastNames.add(jobj.get("last_name").getAsString());
                }
            });
        }

        return lastNames;
    }

    /**
     * Run a last name candidates task
     * @param params request parameters
     */
    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {

        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

        User user = new User(userId);

        ArrayList<ICandidate> candidates = null;

        HashMap<IFact, String> lastNameFacts = dataSource.obtainSpecificFactForUser(factory, user, lastNameFactName);

        HashMap<String, Double> cands = new HashMap<>();

        for (IFact fact : lastNameFacts.keySet()) {
            String value = lastNameFacts.get(fact);
            if (value.equals(""))
                continue;
            if (cands.containsKey(value))
                cands.put(value, cands.get(value) + 1.0);
            else
                cands.put(value, 1.0);
        }

        // get facebook family
        JsonArray fbkFamily = this.mongo.getFacebookFamily(factory, user);

        ArrayList<String> directRelativesLastName = obtainDirectRelativesLastNames(fbkFamily);

        for (String relativeLastName : directRelativesLastName) {
            if (relativeLastName.equals(""))
                continue;
            if (cands.containsKey(relativeLastName))
                cands.put(relativeLastName, cands.get(relativeLastName) + 1.0);
            else
                cands.put(relativeLastName, 1.0);
        }

        JsonObject fbkProfile = this.mongo.getFacebookProfile(factory, user);
        if (LocalUtils.validateJsonField(fbkProfile, "email")) {
            String fbkEmail = fbkProfile.get("email").getAsString();
            searchForStringInCandidates(cands, fbkEmail);
        }

        JsonObject amzProfile = this.mongo.getAmazonProfile(factory, user);
        if (LocalUtils.validateJsonField(amzProfile, "email")) {
            String amzEmail = amzProfile.get("email").getAsString();
            searchForStringInCandidates(cands, amzEmail);
        }

        JsonObject dropboxProfile = this.mongo.getDropboxProfile(factory, user);
        if (LocalUtils.validateJsonField(dropboxProfile, "email")) {
            String dropboxEmail = dropboxProfile.get("email").getAsString();
            searchForStringInCandidates(cands, dropboxEmail);
        }

        JsonObject googleProfile = this.mongo.getGoogleProfile(factory, user);
        if (LocalUtils.validateJsonField(googleProfile, "email")) {
            String googleEmail = googleProfile.get("email").getAsString();
            searchForStringInCandidates(cands, googleEmail);
        }

        JsonObject linkedinProfile = this.mongo.getLinkedinProfile(factory, user);
        if (LocalUtils.validateJsonField(linkedinProfile, "emailAddress")) {
            String linkedinEmail = linkedinProfile.get("emailAddress").getAsString();
            searchForStringInCandidates(cands, linkedinEmail);
        }

        JsonObject twitterProfile = this.mongo.getTwitterProfile(factory, user);
        if (LocalUtils.validateJsonField(twitterProfile, "screen_name")) {
            String twitterScreenName = twitterProfile.get("screen_name").getAsString();
            searchForStringInCandidates(cands, twitterScreenName);
        }

        JsonObject paypalProfile = this.mongo.getPaypalProfile(factory, user);
        if (LocalUtils.validateJsonField(paypalProfile, "name") && LocalUtils
                .validateJsonField(paypalProfile, "email")) {
            String paypalName = paypalProfile.get("name").getAsString();
            // break the name:
            String[] parts = paypalName.split(" ");
            searchForStringInCandidates(cands, parts[parts.length - 1]);
            String paypalEmail = paypalProfile.get("email").getAsString();
            searchForStringInCandidates(cands, paypalEmail);
        }

        candidates = LocalUtils.normalizeCandidatesScores(cands);

        // save to the database the best candidate value
        if (candidates.size() > 0) {
            dataSource.insertAttributeCandidatesForUser(factory, user, "lastName", candidates);
            //     if (verbose)
            logger.info(String.format(
                    "Last name candidate extractor found best candidate: %s with support %.2f for user %s",
                    candidates.get(0).getValue(), candidates.get(0).getSupportScore(), userId));
        } else if (verbose)
            logger.info(String.format("Last name candidate extractor found no candidates for user %s", userId));

    }
}
