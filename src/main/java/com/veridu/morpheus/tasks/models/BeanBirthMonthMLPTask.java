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
@Component("birthmonth-mlp")
public class BeanBirthMonthMLPTask implements ITask {

    private IUtils utils;

    private IDataSource dao;

    private IFeatureExtractor birthMonthFeatureExtractor;

    private static final IFact fact = new Fact("probRealBirthMonthMLP", Constants.SKYNET_PROVIDER);

    private static final Logger log = Logger.getLogger(BeanBirthMonthMLPTask.class);

    @Autowired
    public BeanBirthMonthMLPTask(IUtils utils, IDataSource dao,
            @Qualifier("birthMonthExtractor") IFeatureExtractor birthMonthFeatureExtractor) {
        this.utils = utils;
        this.dao = dao;
        this.birthMonthFeatureExtractor = birthMonthFeatureExtractor;
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

        if (utils.checkIfCandidatesExist(factory, user, "birthMonth")) {

            IModel model = utils.readModel("/models/" + Constants.BIRTH_MONTH_MLP_MODEL_NAME);
            Instances datasetHeader = this.utils
                    .generateDatasetHeader(this.birthMonthFeatureExtractor.obtainFactList());

            long time2, timediff = 0;

            Instance inst = this.birthMonthFeatureExtractor.createInstance(factory, datasetHeader, user);

            IPrediction pred = null;
            double realUserProb = -1;

            try {
                pred = model.predict(inst);
                realUserProb = pred.realUserProbability();

                dao.upsertScore(factory, user, "birthMonthScore", "birthMonth", realUserProb);

                if (realUserProb >= 0.9999658) {
                    dao.upsertGate(factory, user, "birthMonthGate", "high"); // high
                } else if (realUserProb >= 0.9994298) {
                    dao.upsertGate(factory, user, "birthMonthGate", "medium"); // med
                } else if (realUserProb >= 0.7290693) {
                    dao.upsertGate(factory, user, "birthMonthGate", "low"); // low
                }

                time2 = System.currentTimeMillis();
                timediff = time2 - time1;

                //            if (params.verbose)
                log.info(String.format("Birthmonth MLP model predicted real probability for user %s => %.2f in %d ms",
                        userId, pred.realUserProbability(), time2 - time1));

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (pred == null)
                log.error("Birthmonth MLP model could not make prediction for user " + user.getId());

        } else {
            dao.upsertGate(factory, user, "birthMonthGate", "none");

            log.info(String.format("Birthmonth MLP model found no candidates to score for user %s", userId));
        }
    }
}
