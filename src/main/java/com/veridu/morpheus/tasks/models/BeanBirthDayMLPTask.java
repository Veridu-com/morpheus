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
@Component("birthday-mlp")
public class BeanBirthDayMLPTask implements ITask {

    private IUtils utils;

    private IDataSource dao;

    private IFeatureExtractor birthDayFeatureExtractor;

    private static final Logger log = Logger.getLogger(BeanBirthDayMLPTask.class);

    @Autowired
    public BeanBirthDayMLPTask(IUtils utils, IDataSource dao,
            @Qualifier("birthDayExtractor") IFeatureExtractor birthDayFeatureExtractor) {
        this.utils = utils;
        this.dao = dao;
        this.birthDayFeatureExtractor = birthDayFeatureExtractor;
    }

    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {
        long time1 = System.currentTimeMillis();

        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;
        IUser user = new User(userId);

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

        if (utils.checkIfCandidatesExist(factory, user, "birthDay")) {
            IModel model = utils.readModel("/models/" + Constants.BIRTH_DAY_MLP_MODEL_NAME);

            Instances datasetHeader = this.utils.generateDatasetHeader(this.birthDayFeatureExtractor.obtainFactList());

            long time2, timediff = 0;

            Instance inst = this.birthDayFeatureExtractor.createInstance(factory, datasetHeader, user);

            IPrediction pred = null;
            double realUserProb = -1;

            try {
                pred = model.predict(inst);
                realUserProb = pred.realUserProbability();

                dao.upsertScore(factory, user, "birthDayScore", "birthDay", realUserProb);

                dao.upsertGate(factory, user, "birthDayGate", realUserProb >= 0.7958683, "low"); // low gate
                dao.upsertGate(factory, user, "birthDayGate", realUserProb >= 0.9949294, "medium"); // med gate
                dao.upsertGate(factory, user, "birthDayGate", realUserProb >= 0.9991777, "high"); // high gate

                time2 = System.currentTimeMillis();
                timediff = time2 - time1;

                //            if (params.verbose)
                log.info(String.format("Birthday MLP model predicted real probability for user %s => %.2f in %d ms",
                        userId, pred.realUserProbability(), time2 - time1));

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (pred == null)
                log.error("Birthday MLP model could not make prediction for user " + user.getId());
        } else {
            log.info(String.format("Birthday MLP model found no candidates to score for user %s", userId));
        }
    }
}
