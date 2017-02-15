/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.tasks.candidates;

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
@Component("city-candidates")
public class CityCandidateTask implements ITask {

    private IDataSource dataSource;

    private IUtils utils;

    private IMongoDataSource mongo;

    private static final Logger logger = Logger.getLogger(CityCandidateTask.class);

    @Autowired
    public CityCandidateTask(IDataSource dataSource, IUtils utils, IMongoDataSource mongo) {
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

        User user = new User(userId);

        HashMap<IFact, String> userFacts = dataSource.obtainSpecificFactForUser(factory, user, "*city*");

        HashMap<String, Double> cands = new HashMap<>();

        for (IFact fact : userFacts.keySet()) {
            String value = userFacts.get(fact).toLowerCase();
            if (value.equals(""))
                continue;
            if (!cands.containsKey(value))
                cands.put(value, 1.0);
            else
                cands.put(value, cands.get(value) + 1.0);
        }

        // normalize candidates
        candidates = LocalUtils.normalizeCandidatesScores(cands);

        // save to the database the best birth year candidate value
        if (candidates.size() > 0) {
            dataSource.insertAttributeCandidatesForUser(factory, user, "cityName", candidates);
            //  if (verbose)
            logger.info(String.format("City candidate extractor found best candidate: %s with support %.2f for user %s",
                    candidates.get(0).getValue(), candidates.get(0).getSupportScore(), userId));
        } else if (verbose)
            logger.info(String.format("City candidate extractor found no candidates for user %s", userId));

    }

}
