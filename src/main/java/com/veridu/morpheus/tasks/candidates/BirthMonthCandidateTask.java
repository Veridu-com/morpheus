/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.tasks.candidates;

import com.veridu.idos.IdOSAPIFactory;
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
@Component("birthmonth-candidates")
public class BirthMonthCandidateTask implements ITask {

    private IDataSource dataSource;

    private IUtils utils;

    private IMongoDataSource mongo;

    private static final IFact candidateFact = new Fact("bestCandidateBirthMonth", "skynet");

    private static final String bMonthFactName = "birthMonth";

    private static final Logger logger = Logger.getLogger(BirthMonthCandidateTask.class);

    /**
     * Constructor
     * @param utils injected utils bean
     * @param dataSource injected idOS SQL data source
     * @param mongo injected idOS NoSQL data source
     */
    @Autowired
    public BirthMonthCandidateTask(IDataSource dataSource, IUtils utils, IMongoDataSource mongo) {
        this.dataSource = dataSource;
        this.utils = utils;
        this.mongo = mongo;
    }

    /**
     * Run a birthmonth candidates task
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

        HashMap<String, Double> cands = new HashMap<>();

        for (IFact fact : userFacts.keySet())
            if (fact.getName().equals(bMonthFactName)) {
                String value = userFacts.get(fact);
                if (value.equals("0"))
                    continue;
                if (!cands.containsKey(value))
                    cands.put(value, 1.0);
                else
                    cands.put(value, cands.get(value) + 1.0);
            }

        candidates = LocalUtils.normalizeCandidatesScores(cands);

        // save to the database the best birth year candidate value
        if (candidates.size() > 0) {
            dataSource.insertAttributeCandidatesForUser(factory, user, "birthMonth", candidates);
            //    if (verbose)
            logger.info(String.format(
                    "Birth month candidate extractor found best candidate: %s with support %.2f for user %s",
                    candidates.get(0).getValue(), candidates.get(0).getSupportScore(), userId));
        } else if (verbose)
            logger.info(String.format("Birth month candidate extractor found no candidates for user %s", userId));

    }

}
