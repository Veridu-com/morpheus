package com.veridu.morpheus.tasks.candidates;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
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
@Component("profilepic-candidates")
public class ProfilePictureCandidateTask implements ITask {

    private IUtils utils;

    private IDataSource dataSource;

    private static final String profilePicFactName = "profilePicture";

    private static final Logger logger = Logger.getLogger(ProfilePictureCandidateTask.class);

    private static final IFact fbkPic = new Fact(profilePicFactName, Constants.FACEBOOK_PROVIDER_NAME);
    private static final IFact linPic = new Fact(profilePicFactName, Constants.LINKEDIN_PROVIDER_NAME);
    private static final IFact gooPic = new Fact(profilePicFactName, Constants.GOOGLE_PROVIDER_NAME);
    private static final IFact twiPic = new Fact(profilePicFactName, Constants.TWITTER_PROVIDER_NAME);

    @Autowired
    public ProfilePictureCandidateTask(IUtils utils, IDataSource dataSource) {
        this.utils = utils;
        this.dataSource = dataSource;
    }

    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {
        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

        ArrayList<ICandidate> candidates = null;

        IUser user = new User(userId);

        HashMap<IFact, String> userFacts = dataSource.obtainSpecificFactForUser(factory, user, "profilePicture");

        HashMap<String, Double> cands = new HashMap<>();

        if (userFacts.containsKey(fbkPic))
            cands.put(userFacts.get(fbkPic), 4.0);
        if (userFacts.containsKey(linPic))
            cands.put(userFacts.get(linPic), 3.0);
        if (userFacts.containsKey(gooPic))
            cands.put(userFacts.get(gooPic), 2.0);
        if (userFacts.containsKey(twiPic))
            cands.put(userFacts.get(twiPic), 1.0);

        candidates = LocalUtils.normalizeCandidatesScores(cands);

        // save to the database the best birth year candidate value
        if (candidates.size() > 0) {
            dataSource.insertAttributeCandidatesForUser(factory, user, profilePicFactName, candidates);
            if (verbose)
                logger.info(String.format(
                        "Profile picture candidate extractor found best candidate: %s with support %.2f for user %s",
                        candidates.get(0).getValue(), candidates.get(0).getSupportScore(), userId));
        } else if (verbose)
            logger.info(String.format("Profile picture candidate extractor found no candidates for user %s", userId));

    }
}
