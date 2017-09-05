/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.tasks.flags;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IMongoDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.Parameters;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created by cassio on 4/20/17.
 */
@Component("flags-non-original-content")
public class NonOriginalContentFlagTask implements ITask {

    private IUtils utils;

    private IDataSource dao;

    private IMongoDataSource nosql;

    private static final Logger log = Logger.getLogger(NonOriginalContentFlagTask.class);

    private static final String FLAG_AUTO_POSTING = "non-original-content-flag";

    /**
     * Constructor
     * @param utils injected utils bean
     * @param dao injected idOS SQL data source
     * @param nosql injected idOS NoSQL data source
     */
    @Autowired
    public NonOriginalContentFlagTask(IUtils utils, IDataSource dao, IMongoDataSource nosql) {
        this.utils = utils;
        this.dao = dao;
        this.nosql = nosql;
    }

    /**
     * Run an automated posting flag task
     * @param params request parameters
     */
    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {
        long time1 = System.currentTimeMillis(); // START CLOCK

        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

        IUser user = new User(userId);

        //        this.dao.deleteFlag(factory, user, FLAG_AUTO_POSTING);
        //
        //        JsonArray tweets = this.nosql.getTweets(factory, user);
        //
        //        boolean automatedPosting = false;
        //
        //        if (tweets != null && tweets.size() >= (Constants.NUM_TWEETS_AUTO_POSTING_THRESHOLD + 1)) {
        //
        //            Date previousTweetDate = this.utils
        //                    .parseTwitterUTC(tweets.get(0).getAsJsonObject().get("created_at").getAsString());
        //
        //            int quickTweetsCount = 0;
        //
        //            for (int i = 1; i < tweets.size(); i++) {
        //                Date currentTweetDate = this.utils
        //                        .parseTwitterUTC(tweets.get(i).getAsJsonObject().get("created_at").getAsString());
        //
        //                if (this.utils.computeDateDiffInSeconds(previousTweetDate, currentTweetDate) <= 1)
        //                    quickTweetsCount++;
        //
        //                previousTweetDate = currentTweetDate;
        //
        //                if (quickTweetsCount >= Constants.NUM_TWEETS_AUTO_POSTING_THRESHOLD) {
        //                    automatedPosting = true;
        //                    break;
        //                }
        //            }
        //
        //        }
        //
        //        if (automatedPosting)
        //            this.dao.insertFlag(factory, user, FLAG_AUTO_POSTING, Constants.PROFILE_PROVIDER_NAME);
        //
        //        long time2 = System.currentTimeMillis(); // STOP CLOCK
        //
        //        if (verbose)
        //            log.info(String.format("Automated posting flag returned %s for user %s in %d ms", automatedPosting, userId,
        //                    time2 - time1));
    }

}
