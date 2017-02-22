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
@Component("country-mlp")
public class BeanCountryMLPTask implements ITask {

    private IFeatureExtractor countryFeatureExtractor;

    private IUtils utils;

    private IDataSource dao;

    private static final IFact fact = new Fact("probRealCountryMLP", "skynet");

    private static final Logger log = Logger.getLogger(BeanCountryMLPTask.class);

    /**
     * Constructor
     *
     * @param countryFeatureExtractor injected country feature extractor
     * @param utils injected utils bean
     * @param dao injected idOS SQL data source
     */
    @Autowired
    public BeanCountryMLPTask(@Qualifier("countryExtractor") IFeatureExtractor countryFeatureExtractor, IUtils utils,
            IDataSource dao) {
        this.countryFeatureExtractor = countryFeatureExtractor;
        this.utils = utils;
        this.dao = dao;
    }

    /**
     * Run a country prediction task
     *
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

        if (utils.checkIfCandidatesExist(factory, user, "countryName")) {

            IModel model = utils.readModel("/models/" + Constants.COUNTRY_MLP_MODEL_NAME);
            Instances datasetHeader = this.utils.generateDatasetHeader(this.countryFeatureExtractor.obtainFactList());

            long time2, timediff = 0;

            Instance inst = this.countryFeatureExtractor.createInstance(factory, datasetHeader, user);

            IPrediction pred = null;
            double realUserProb = -1;

            try {
                pred = model.predict(inst);
                realUserProb = pred.realUserProbability();

                dao.upsertScore(factory, user, "countryNameScore", "countryName", realUserProb);

                if (realUserProb >= 0.9999494) {
                    dao.upsertGate(factory, user, "countryNameGate", "high");
                } else if (realUserProb >= 0.9994642) {
                    dao.upsertGate(factory, user, "countryNameGate", "medium");
                } else if (realUserProb >= 0.99) {
                    dao.upsertGate(factory, user, "countryNameGate", "low");
                } else {
                    dao.upsertGate(factory, user, "countryNameGate", "none");
                }

                time2 = System.currentTimeMillis();
                timediff = time2 - time1;

                //            if (params.verbose)
                log.info(String.format("Country MLP model predicted real probability for user %s => %.2f in %d ms",
                        userId, pred.realUserProbability(), time2 - time1));

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (pred == null)
                log.error("Country MLP model could not make prediction for user " + user.getId());
        } else {
            dao.upsertGate(factory, user, "countryNameGate", "NA");

            log.info(String.format("Country MLP model found no candidates to score for user %s", userId));
        }
    }
}
