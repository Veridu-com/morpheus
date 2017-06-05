package com.veridu.morpheus.tasks.flags;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.Parameters;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cassio on 4/19/17.
 */
@Component("flags-name-mismatch")
public class NameMismatchFlagTask implements ITask {

    private IUtils utils;

    private IDataSource dao;

    /**
     * Constructor
     * @param utils injected utils bean
     * @param dao injected idOS SQL data source
     */
    @Autowired
    public NameMismatchFlagTask(IUtils utils, IDataSource dao) {
        this.utils = utils;
        this.dao = dao;
    }

    private static final Logger log = Logger.getLogger(NameMismatchFlagTask.class);

    private static final String firstNameFact = "firstName";
    private static final String lastNameFact = "lastName";

    private static final String FLAG_FIRST_NAME_MISMATCH = "firstname-mismatch";
    private static final String FLAG_LAST_NAME_MISMATCH = "lastname-mismatch";

    public boolean hasNameMismatch(IUser user, IdOSAPIFactory factory, String factName) {
        HashMap<IFact, String> providerFacts = this.dao.obtainSpecificFactForUser(factory, user, factName);

        if (providerFacts.size() < 2)
            return false;

        NormalizedLevenshtein leven = new NormalizedLevenshtein();

        ArrayList<IFact> facts = new ArrayList<>(providerFacts.keySet());

        String firstString = providerFacts.get(facts.get(0));

        for (int i = 1; i < facts.size(); i++) {
            String currentString = providerFacts.get(facts.get(i));
            if (leven.similarity(currentString, firstString) < Constants.STRING_SIMILARITY_THRESHOLD)
                return true;
        }

        return false;
    }

    /**
     * Run a new profile flag task
     * @param params request parameters
     */
    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {
        long time1 = System.currentTimeMillis(); // START CLOCK

        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;

        IUser user = new User(userId);

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

        this.dao.deleteFlag(factory, user, FLAG_FIRST_NAME_MISMATCH);
        this.dao.deleteFlag(factory, user, FLAG_LAST_NAME_MISMATCH);

        boolean firstNameMismatch = hasNameMismatch(user, factory, firstNameFact);
        boolean lastNameMismatch = hasNameMismatch(user, factory, lastNameFact);

        if (firstNameMismatch)
            this.dao.insertFlag(factory, user, FLAG_FIRST_NAME_MISMATCH, Constants.PROFILE_PROVIDER_NAME);

        if (lastNameMismatch)
            this.dao.insertFlag(factory, user, FLAG_LAST_NAME_MISMATCH, Constants.PROFILE_PROVIDER_NAME);

        long time2 = System.currentTimeMillis(); // STOP CLOCK

        if (verbose)
            log.info(String.format("First name flag returned %s; last name flag returned %s for user %s in %d ms",
                    firstNameMismatch, lastNameMismatch, userId, time2 - time1));
    }

}
