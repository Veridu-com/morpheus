package com.veridu.morpheus.tasks.models;

import com.google.gson.JsonObject;
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

        IBinaryModel model = (IBinaryModel) utils
                .readModel("/models/" + Constants.OVERALL_COST_SENSITIVE_NB_MODEL_NAME);

        Instances datasetHeader = this.utils.generateDatasetHeader(this.overallExtractor.obtainFactList());

        long time2, timediff = 0;

        IUser user = new User(userId);
        double realUserProb = -1;

        try {
            IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

            Instance inst = this.overallExtractor.createInstance(factory, datasetHeader, user);
            System.out.println("---------------------------------------");
            System.out.println("Header:");
            System.out.println(datasetHeader);
            System.out.println("generated instance:");
            System.out.println(inst);
            System.out.println("---------------------------------------");

            double score = model.getClassifier().distributionForInstance(inst)[1];
            realUserProb = model.binaryPrediction(inst);

            dao.upsertScore(factory, user, "overall-score-series-s-model-csnb", "profile", score);
            dao.upsertGate(factory, user, "chargeback", realUserProb > 0.99);

            time2 = System.currentTimeMillis();
            timediff = time2 - time1;

            log.info(String.format(
                    "Overall Cost Sensitive Naive Bayes model predicted for user %s => %.2f with probability %.2f in %d ms",
                    userId, realUserProb, score, time2 - time1));

        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObject responseBuilder = new JsonObject();

        responseBuilder.addProperty(Constants.MODEL_NAME_RESPONSE_STR, Constants.OVERALL_COST_SENSITIVE_NB_MODEL_NAME);
        responseBuilder.addProperty(Constants.USER_ID_RESPONSE_STR, userId);
        responseBuilder.addProperty(Constants.REAL_USR_PROB_RESPONSE_STR, realUserProb);
        responseBuilder.addProperty(Constants.TIME_TAKEN_RESPONSE_STR, timediff);

        if (params.verbose)
            System.out.println(responseBuilder);
    }
}
