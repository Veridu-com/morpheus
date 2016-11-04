package com.veridu.morpheus.tasks.models;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IFeatureExtractor;
import com.veridu.morpheus.interfaces.beans.IUtils;
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
@Component("email-mlp")
public class BeanEmailMLPTask implements ITask {

    private IFeatureExtractor emailFeatureExtractor;

    private IUtils utils;

    private IDataSource dao;

    private static final Logger log = Logger.getLogger(BeanEmailMLPTask.class);

    @Autowired
    public BeanEmailMLPTask(@Qualifier("emailExtractor") IFeatureExtractor emailFeatureExtractor, IUtils utils,
            IDataSource dao) {
        this.emailFeatureExtractor = emailFeatureExtractor;
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

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));
        IUser user = new User(userId);

        if (utils.checkIfCandidatesExist(factory, user, "email")) {

            IModel model = utils.readModel("/models/" + Constants.EMAIL_MLP_MODEL_NAME);
            Instances datasetHeader = this.utils.generateDatasetHeader(this.emailFeatureExtractor.obtainFactList());

            long time2, timediff = 0;

            Instance inst = this.emailFeatureExtractor.createInstance(factory, datasetHeader, user);

            IPrediction pred = null;

            double realUserProb = -1;

            try {
                pred = model.predict(inst);
                realUserProb = pred.realUserProbability();

                dao.upsertScore(factory, user, "email-score-series-s-model-m", "email", realUserProb);

                dao.upsertGate(factory, user, "email-gate-low", realUserProb >= 0.99);
                dao.upsertGate(factory, user, "email-gate-med", realUserProb >= 0.9994281);
                dao.upsertGate(factory, user, "email-gate-high", realUserProb >= 0.9998348);

                time2 = System.currentTimeMillis();
                timediff = time2 - time1;

                //            if (params.verbose)
                log.info(
                        String.format("Email MLP model predicted real probability for user %s => %.2f in %d ms", userId,
                                pred.realUserProbability(), time2 - time1));

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (pred == null)
                log.error("Email MLP model could not make prediction for user " + user.getId());

        } else {
            log.info(String.format("Email MLP model found no candidates to score for user %s", userId));
        }
    }
}
