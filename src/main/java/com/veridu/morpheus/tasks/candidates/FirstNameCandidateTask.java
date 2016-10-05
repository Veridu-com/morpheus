package com.veridu.morpheus.tasks.candidates;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IMongoDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.ICandidate;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.interfaces.users.IUser;
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
@Component("firstname-candidates")
public class FirstNameCandidateTask implements ITask {

    private IDataSource dataSource;

    private IUtils utils;

    private IMongoDataSource mongo;

    private static final String firstNameFactName = "firstName";

    private static final IFact amaFirstNameFact = new Fact(firstNameFactName, Constants.AMAZON_PROVIDER_NAME);
    private static final IFact twiFirstNameFact = new Fact(firstNameFactName, Constants.TWITTER_PROVIDER_NAME);
    private static final IFact payFirstNameFact = new Fact(firstNameFactName, Constants.PAYPAL_PROVIDER_NAME);
    private static final IFact linFirstNameFact = new Fact(firstNameFactName, Constants.LINKEDIN_PROVIDER_NAME);
    private static final IFact gooFirstNameFact = new Fact(firstNameFactName, Constants.GOOGLE_PROVIDER_NAME);
    private static final IFact fbkFirstNameFact = new Fact(firstNameFactName, Constants.FACEBOOK_PROVIDER_NAME);

    Logger logger = Logger.getLogger(FirstNameCandidateTask.class);

    @Autowired
    public FirstNameCandidateTask(IDataSource dataSource, IUtils utils, IMongoDataSource mongo) {
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

        IUser user = new User(userId);

        HashMap<IFact, String> firstNameFacts = dataSource.obtainSpecificFactForUser(factory, user, firstNameFactName);

        String[] firstNameList = { "-1", "-1", "-1", "-1", "-1", "-1" };

        firstNameList[0] = firstNameFacts.containsKey(amaFirstNameFact) ? firstNameFacts.get(amaFirstNameFact) : null;
        firstNameList[1] = firstNameFacts.containsKey(twiFirstNameFact) ?
                firstNameFacts.get(twiFirstNameFact).toString() :
                null;
        firstNameList[2] = firstNameFacts.containsKey(payFirstNameFact) ?
                firstNameFacts.get(payFirstNameFact).toString() :
                null;
        firstNameList[3] = firstNameFacts.containsKey(linFirstNameFact) ?
                firstNameFacts.get(linFirstNameFact).toString() :
                null;
        firstNameList[4] = firstNameFacts.containsKey(gooFirstNameFact) ?
                firstNameFacts.get(gooFirstNameFact).toString() :
                null;
        firstNameList[5] = firstNameFacts.containsKey(fbkFirstNameFact) ?
                firstNameFacts.get(fbkFirstNameFact).toString() :
                null;

        // unify candidates
        HashMap<String, Double> cands = new HashMap<>();

        for (int i = 0; i < firstNameList.length; i++) {
            if ((firstNameList[i] == null) || firstNameList[i].equals(""))
                continue;
            String val = String.valueOf(firstNameList[i]);
            if (!cands.containsKey(val))
                cands.put(val, 1.0);
            else
                cands.put(val, cands.get(val) + 1.0);
        }

        candidates = LocalUtils.normalizeCandidatesScores(cands);

        // save to the database the best candidate value
        if (candidates.size() > 0) {
            dataSource.insertAttributeCandidatesForUser(factory, user, "first-name", candidates);
            if (verbose)
                logger.info(String.format(
                        "First name candidate extractor found best candidate: %s with support %.2f for user %s",
                        candidates.get(0).getValue(), candidates.get(0).getSupportScore(), userId));
        } else if (verbose)
            logger.info(String.format("First name candidate extractor found no candidates for user %s", userId));

    }
}
