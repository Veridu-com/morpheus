package com.veridu.morpheus.tasks.models;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IFeatureExtractor;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.models.IBinaryModel;
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
@Component("overall-cs-nb")
public class BeanOverallCostSensitiveNaiveBayesTask implements ITask {

    private IUtils utils;

    private IDataSource dao;

    private IFeatureExtractor overallExtractor;

    private static final Logger log = Logger.getLogger(BeanOverallCostSensitiveNaiveBayesTask.class);

    private static final boolean DEBUG = false;

    @Autowired
    public BeanOverallCostSensitiveNaiveBayesTask(IUtils utils, IDataSource dao,
            @Qualifier("overallExtractor") IFeatureExtractor overallExtractor) {
        this.utils = utils;
        this.dao = dao;
        this.overallExtractor = overallExtractor;
    }

    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {
        long time1 = System.currentTimeMillis();

        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;

        IBinaryModel modelLow = (IBinaryModel) utils
                .readModel("/models/" + Constants.OVERALL_COST_SENSITIVE_NB_LOW_MODEL_NAME);

        IBinaryModel modelMed = (IBinaryModel) utils
                .readModel("/models/" + Constants.OVERALL_COST_SENSITIVE_NB_MED_MODEL_NAME);

        IBinaryModel modelHigh = (IBinaryModel) utils
                .readModel("/models/" + Constants.OVERALL_COST_SENSITIVE_NB_HIGH_MODEL_NAME);

        Instances datasetHeader = this.utils.generateDatasetHeader(this.overallExtractor.obtainFactList());

        long time2, timediff = 0;

        IUser user = new User(userId);

        try {
            IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

            Instance inst = this.overallExtractor.createInstance(factory, datasetHeader, user);

            if (DEBUG) {
                System.out.println("---------------------------------------");
                System.out.println("Header:");
                System.out.println(datasetHeader);
                System.out.println("generated instance:");
                System.out.println(inst);
                System.out.println("---------------------------------------");
            }

            // low
            double score = modelLow.getClassifier().distributionForInstance(inst)[1];
            double realUserProbLow = modelLow.binaryPrediction(inst);
            dao.upsertScore(factory, user, "overall-score-series-s-model-csnb-low", "profile", score);
            dao.upsertGate(factory, user, "chargeback-low", realUserProbLow > 0.99);

            // med
            score = modelMed.getClassifier().distributionForInstance(inst)[1];
            double realUserProbMed = modelMed.binaryPrediction(inst);
            dao.upsertScore(factory, user, "overall-score-series-s-model-csnb-med", "profile", score);
            dao.upsertGate(factory, user, "chargeback-med", realUserProbMed > 0.99);

            // high
            score = modelHigh.getClassifier().distributionForInstance(inst)[1];
            double realUserProbHigh = modelHigh.binaryPrediction(inst);
            dao.upsertScore(factory, user, "overall-score-series-s-model-csnb-high", "profile", score);
            dao.upsertGate(factory, user, "chargeback-high", realUserProbHigh > 0.99);

            time2 = System.currentTimeMillis();
            timediff = time2 - time1;

            if (params.verbose) {
                log.info(String.format(
                        "Overall Cost Sensitive Naive Bayes LOW model predicted for user %s => %.2f with probability %.2f in %d ms",
                        userId, realUserProbLow, score, time2 - time1));
                log.info(String.format(
                        "Overall Cost Sensitive Naive Bayes MED model predicted for user %s => %.2f with probability %.2f in %d ms",
                        userId, realUserProbMed, score, time2 - time1));
                log.info(String.format(
                        "Overall Cost Sensitive Naive Bayes HIGH model predicted for user %s => %.2f with probability %.2f in %d ms",
                        userId, realUserProbHigh, score, time2 - time1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
