/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.tasks.models;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IFeatureExtractor;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.models.IModel;
import com.veridu.morpheus.interfaces.models.IPrediction;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.Parameters;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by cassio on 10/4/16.
 */
@Component("birthyear-mlp")
public class BeanBirthYearMLPTask implements ITask {

    private IFeatureExtractor birthYearFeatureExtractor;

    private IUtils utils;

    private IDataSource dao;

    private static final IFact fact = new Fact("probRealBirthYearMLP", "skynet");

    private static final Logger log = Logger.getLogger(BeanBirthYearMLPTask.class);

    /**
     * Constructor
     *
     * @param utils injected utils bean
     * @param dao injected idOS SQL data source
     * @param birthYearFeatureExtractor injected birthyear feature extractor
     */
    @Autowired
    public BeanBirthYearMLPTask(@Qualifier("birthYearExtractor") IFeatureExtractor birthYearFeatureExtractor,
            IUtils utils, IDataSource dao) {
        this.birthYearFeatureExtractor = birthYearFeatureExtractor;
        this.utils = utils;
        this.dao = dao;
    }

    /**
     * Run a birth day prediction task
     * @param params request parameters
     */
    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {
        long time1 = System.currentTimeMillis();

        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));
        IUser user = new User(userId);

        if (utils.checkIfCandidatesExist(factory, user, "birthYear")) {

            IModel model = utils.readModel("/models/" + Constants.BIRTH_YEAR_MLP_MODEL_NAME);

            Instances datasetHeader = this.utils.generateDatasetHeader(this.birthYearFeatureExtractor.obtainFactList());

            long time2, timediff = 0;

            Instance inst = this.birthYearFeatureExtractor.createInstance(factory, datasetHeader, user);

            IPrediction pred = null;
            double realUserProb = -1;

            try {
                pred = model.predict(inst);
                realUserProb = pred.realUserProbability();

                dao.upsertScore(factory, user, "birthYearScore", "birthYear", realUserProb);

                if (realUserProb >= 0.9954738) {
                    dao.upsertGate(factory, user, "birthYearGate", "high"); // high
                } else if (realUserProb >= 0.8582320) {
                    dao.upsertGate(factory, user, "birthYearGate", "medium"); // med
                } else if (realUserProb >= 0.7329271) {
                    dao.upsertGate(factory, user, "birthYearGate", "low"); // low
                } else {
                    dao.upsertGate(factory, user, "birthYearGate", "none");
                }

                time2 = System.currentTimeMillis();
                timediff = time2 - time1;

                //            if (params.verbose)
                log.info(String.format("Birthyear MLP model predicted real probability for user %s => %.2f in %d ms",
                        userId, pred.realUserProbability(), time2 - time1));

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (pred == null)
                log.error("Birthyear MLP model could not make prediction for user " + user.getId());
        } else {
            dao.upsertGate(factory, user, "birthYearGate", "NA");

            log.info(String.format("Birthyear MLP model found no candidates to score for user %s", userId));
        }
    }
}
