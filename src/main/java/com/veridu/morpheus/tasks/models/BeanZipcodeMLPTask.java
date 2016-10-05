package com.veridu.morpheus.tasks.models;

import com.google.gson.JsonObject;
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
@Component("zipcode-mlp")
public class BeanZipcodeMLPTask implements ITask {

    private IFeatureExtractor zipcodeFeatureExtractor;

    private IUtils utils;

    private IDataSource dao;

    private static final IFact fact = new Fact("probRealZipcodeMLP", "skynet");

    private static final Logger log = Logger.getLogger(BeanZipcodeMLPTask.class);

    @Autowired
    public BeanZipcodeMLPTask(@Qualifier("zipcodeExtractor") IFeatureExtractor zipcodeFeatureExtractor, IUtils utils,
            IDataSource dao) {
        this.zipcodeFeatureExtractor = zipcodeFeatureExtractor;
        this.utils = utils;
        this.dao = dao;
    }

    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {
        long time1 = System.currentTimeMillis();

        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;

        IModel model = utils.readModel("/models/" + Constants.ZIPCODE_MLP_MODEL_NAME);
        Instances datasetHeader = this.utils.generateDatasetHeader(this.zipcodeFeatureExtractor.obtainFactList());

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

        long time2, timediff = 0;

        IUser user = new User(userId);

        Instance inst = this.zipcodeFeatureExtractor.createInstance(factory, datasetHeader, user);

        IPrediction pred = null;
        double realUserProb = -1;

        try {
            pred = model.predict(inst);
            realUserProb = pred.realUserProbability();

            dao.upsertScore(factory, user, "zipcode-score-series-s-model-m", "zipcode", realUserProb);
            dao.upsertGate(factory, user, "zipcode-gate", realUserProb >= 0.5);

            time2 = System.currentTimeMillis();
            timediff = time2 - time1;

            log.info(String.format("Zipcode MLP model predicted real probability for user %s => %.2f in %d ms", userId,
                    pred.realUserProbability(), time2 - time1));

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pred == null)
            log.error("Zipcode MLP model could not make prediction");

        JsonObject responseBuilder = new JsonObject();

        responseBuilder.addProperty(Constants.MODEL_NAME_RESPONSE_STR, Constants.ZIPCODE_MLP_MODEL_NAME);
        responseBuilder.addProperty(Constants.USER_ID_RESPONSE_STR, userId);
        responseBuilder.addProperty(Constants.REAL_USR_PROB_RESPONSE_STR, realUserProb);
        responseBuilder.addProperty(Constants.TIME_TAKEN_RESPONSE_STR, timediff);

        if (params.verbose)
            System.out.println(responseBuilder);
    }
}
