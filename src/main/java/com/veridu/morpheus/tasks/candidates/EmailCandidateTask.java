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
@Component("email-candidates")
public class EmailCandidateTask implements ITask {

    private IDataSource dataSource;

    private IUtils utils;

    private IMongoDataSource mongo;

    private static final IFact candidateFact = new Fact("bestCandidateEmail", "skynet");

    private static final String emailFactName = "emailAddress";

    private static final Logger logger = Logger.getLogger(BirthDayCandidateTask.class);

    @Autowired
    public EmailCandidateTask(IDataSource dataSource, IUtils utils, IMongoDataSource mongo) {
        this.dataSource = dataSource;
        this.utils = utils;
        this.mongo = mongo;
    }

    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {

        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;

        ArrayList<ICandidate> candidates = null;

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

        IUser user = new User(userId);

        HashMap<IFact, String> userFacts = dataSource.obtainSpecificFactForUser(factory, user, "emailAddress");

        HashMap<String, Double> cands = new HashMap<>();

        for (IFact fact : userFacts.keySet())
            if (fact.getName().equals(emailFactName)) {
                String value = userFacts.get(fact);
                if (value.equals(""))
                    continue;
                if (!cands.containsKey(value))
                    cands.put(value, 1.0);
                else
                    cands.put(value, cands.get(value) + 1.0);
            }

        candidates = LocalUtils.normalizeCandidatesScores(cands);

        if (candidates.size() > 0) {
            dataSource.insertAttributeCandidatesForUser(factory, user, "email", candidates);
            // if (verbose)
            logger.info(
                    String.format("Email candidate extractor found best candidate: %s with support %.2f for user %s",
                            candidates.get(0).getValue(), candidates.get(0).getSupportScore(), userId));
        } else if (verbose)
            logger.info(String.format("Email candidate extractor found no candidates for user %s", userId));

    }
}
