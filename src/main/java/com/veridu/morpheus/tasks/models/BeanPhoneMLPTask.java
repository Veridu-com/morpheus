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
@Component("phone-mlp")
public class BeanPhoneMLPTask implements ITask {

    private IFeatureExtractor phoneFeatureExtractor;

    private IUtils utils;

    private IDataSource dao;

    private static final Logger log = Logger.getLogger(BeanPhoneMLPTask.class);

    @Autowired
    public BeanPhoneMLPTask(@Qualifier("phoneExtractor") IFeatureExtractor phoneFeatureExtractor, IUtils utils,
            IDataSource dao) {
        this.phoneFeatureExtractor = phoneFeatureExtractor;
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

        if (utils.checkIfCandidatesExist(factory, user, "phone")) {

            IModel model = utils.readModel("/models/" + Constants.PHONE_MLP_MODEL_NAME);
            Instances datasetHeader = this.utils.generateDatasetHeader(this.phoneFeatureExtractor.obtainFactList());

            long time2, timediff = 0;

            Instance inst = this.phoneFeatureExtractor.createInstance(factory, datasetHeader, user);

            IPrediction pred = null;

            double realUserProb = -1;

            try {
                pred = model.predict(inst);
                realUserProb = pred.realUserProbability();

                dao.upsertScore(factory, user, "phoneScore", "phone", realUserProb);

                if (realUserProb >= 0.9998200) {
                    dao.upsertGate(factory, user, "phoneGate", "high");
                } else if (realUserProb >= 0.9960656) {
                    dao.upsertGate(factory, user, "phoneGate", "medium");
                } else if (realUserProb >= 0.99) {
                    dao.upsertGate(factory, user, "phoneGate", "low");
                } else {
                    dao.upsertGate(factory, user, "phoneGate", "none");
                }

                time2 = System.currentTimeMillis();
                timediff = time2 - time1;

                //            if (params.verbose)
                log.info(
                        String.format("Phone MLP model predicted real probability for user %s => %.2f in %d ms", userId,
                                pred.realUserProbability(), time2 - time1));

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (pred == null)
                log.error("Phone MLP model could not make prediction for user " + user.getId());
        } else {
            dao.upsertGate(factory, user, "phoneGate", "NA");

            log.info(String.format("Phone MLP model found no candidates to score for user %s", userId));
        }
    }
}
