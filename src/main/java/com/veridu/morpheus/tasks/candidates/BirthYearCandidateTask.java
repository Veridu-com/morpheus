/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.tasks.candidates;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IMongoDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.ICandidate;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.interfaces.users.IUser;
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
@Component("birthyear-candidates")
public class BirthYearCandidateTask implements ITask {

    private IDataSource dataSource;

    private IUtils utils;

    private IMongoDataSource mongo;

    private static final String bYearFactName = "birthYear";

    private static final IFact cprBirthYearFact = new Fact(bYearFactName, Constants.CPR_PROVIDER_NAME);
    private static final IFact fbkBirthYearFact = new Fact(bYearFactName, Constants.FACEBOOK_PROVIDER_NAME);
    private static final IFact linBirthYearFact = new Fact(bYearFactName, Constants.LINKEDIN_PROVIDER_NAME);
    private static final IFact payBirthYearFact = new Fact(bYearFactName, Constants.PAYPAL_PROVIDER_NAME);
    private static final IFact perBirthYearFact = new Fact(bYearFactName, Constants.PERSONAL_PROVIDER_NAME);
    private static final IFact spoBirthYearFact = new Fact(bYearFactName, Constants.SPOTIFY_PROVIDER_NAME);
    private static final IFact yahBirthYearFact = new Fact(bYearFactName, Constants.YAHOO_PROVIDER_NAME);

    Logger logger = Logger.getLogger(this.getClass());

    /**
     * Constructor
     * @param dataSource injected idOS SQL data source
     * @param utils injected utils bean
     * @param mongo injected idOS NoSQL data source
     */
    @Autowired
    public BirthYearCandidateTask(IDataSource dataSource, IUtils utils, IMongoDataSource mongo) {
        this.dataSource = dataSource;
        this.utils = utils;
        this.mongo = mongo;
    }

    /**
     * Run a birthyear candidates task
     * @param params request parameters
     */
    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {
        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

        ArrayList<ICandidate> candidates = null;

        IUser user = new User(userId);

        HashMap<IFact, String> userFacts = dataSource.obtainSpecificFactForUser(factory, user, "*birth*");

        int[] birthYears = { -1, -1, -1, -1, -1, -1, -1 };

        if (userFacts.containsKey(cprBirthYearFact))
            birthYears[0] = Integer.parseInt(userFacts.get(cprBirthYearFact));
        if (userFacts.containsKey(fbkBirthYearFact))
            birthYears[1] = Integer.parseInt(userFacts.get(fbkBirthYearFact));
        if (userFacts.containsKey(linBirthYearFact))
            birthYears[2] = Integer.parseInt(userFacts.get(linBirthYearFact));
        if (userFacts.containsKey(payBirthYearFact))
            birthYears[3] = Integer.parseInt(userFacts.get(payBirthYearFact));
        if (userFacts.containsKey(perBirthYearFact))
            birthYears[4] = Integer.parseInt(userFacts.get(perBirthYearFact));
        if (userFacts.containsKey(spoBirthYearFact))
            birthYears[5] = Integer.parseInt(userFacts.get(spoBirthYearFact));
        if (userFacts.containsKey(yahBirthYearFact))
            birthYears[6] = Integer.parseInt(userFacts.get(yahBirthYearFact));

        // unify candidates
        HashMap<String, Double> cands = new HashMap<>();

        for (int i = 0; i < birthYears.length; i++) {
            if (birthYears[i] <= 0)
                continue;
            String val = String.valueOf(birthYears[i]);
            if (!cands.containsKey(val))
                cands.put(val, 1.0);
            else
                cands.put(val, cands.get(val) + 1.0);
        }

        candidates = LocalUtils.normalizeCandidatesScores(cands);

        // save to the database the best birth year candidate value
        if (candidates.size() > 0) {
            dataSource.insertAttributeCandidatesForUser(factory, user, "birthYear", candidates);
            //         if (verbose)
            logger.info(String.format(
                    "Birth year candidate extractor found best candidate: %s with support %.2f for user %s",
                    candidates.get(0).getValue(), candidates.get(0).getSupportScore(), userId));
        } else if (verbose)
            logger.info(String.format("Birth year candidate extractor found no candidates for user %s", userId));

    }
}
