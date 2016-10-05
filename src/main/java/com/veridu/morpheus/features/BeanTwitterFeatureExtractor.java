package com.veridu.morpheus.features;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IFeatureExtractor;
import com.veridu.morpheus.interfaces.beans.IMongoDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.users.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cassio on 10/3/16.
 */
@Component("twitterExtractor")
public class BeanTwitterFeatureExtractor implements IFeatureExtractor {

    private IDataSource dataSource;

    private IUtils utils;

    private IMongoDataSource mongo;

    private ArrayList<IFact> basicNumericFacts;

    private ArrayList<IFact> facts;

    private static final boolean DEBUG = false;

    @Autowired
    public BeanTwitterFeatureExtractor(IDataSource dataSource, IUtils utils, IMongoDataSource mongo) {
        this.dataSource = dataSource;
        this.utils = utils;
        this.mongo = mongo;
    }

    @PostConstruct
    public void init() {
        this.basicNumericFacts = this.utils.getTwitterNumericFacts();
        this.facts = new ArrayList<>(basicNumericFacts);

        IFact numOfFollowTrain = new Fact("numOfFollowTrain", "skynet");
        IFact numOfRetweetsMentions = new Fact("numOfRetweetsMentions", "skynet");
        IFact numOfFollowersMentions = new Fact("numOfFollowersMentions", "skynet");
        IFact numOfFollowBacksMentions = new Fact("numOfFollowBacksMentions", "skynet");
        IFact numOfFollowTrickMentions = new Fact("numOfFollowTrickMentions", "skynet");

        this.facts.add(numOfFollowTrain);
        this.facts.add(numOfRetweetsMentions);
        this.facts.add(numOfFollowersMentions);
        this.facts.add(numOfFollowBacksMentions);
        this.facts.add(numOfFollowTrickMentions);
    }

    @Override
    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, IUser user) {

        Instance inst = new DenseInstance(dataset.numAttributes());

        HashMap<IFact, Double> numericFacts = this.dataSource
                .obtainNumericFactsForProfile(factory, user, Constants.TWITTER_PROVIDER_NAME);

        int attPos = 0;

        for (IFact fact : this.basicNumericFacts) {
            if (numericFacts.containsKey(fact)) {
                Double value = numericFacts.get(fact);
                inst.setValue(attPos, value);
            }
            attPos++;
        }

        JsonArray tweets = this.mongo.getTweets(factory, user);

        int numFollowTrain = 0;
        int numRetweetsMentions = 0;
        int numFollowersMentions = 0;
        int numFollowBacksMentions = 0;
        int numFollowTrickMentions = 0;

        if (tweets != null) {
            for (int i = 0; i < tweets.size(); i++) {
                JsonObject tweet = tweets.get(i).getAsJsonObject();
                String txtMessage = tweet.get("text").getAsString().toLowerCase().replaceAll("\\s+", "");
                if (txtMessage.contains("retweet"))
                    numRetweetsMentions++;
                if (txtMessage.contains("followtrain"))
                    numFollowTrain++;
                if (txtMessage.contains("followers"))
                    numFollowersMentions++;
                if (txtMessage.contains("followback"))
                    numFollowBacksMentions++;
                if (txtMessage.contains("followtrick"))
                    numFollowTrickMentions++;
            }
        }

        inst.setValue(attPos++, numFollowTrain);
        inst.setValue(attPos++, numRetweetsMentions);
        inst.setValue(attPos++, numFollowersMentions);
        inst.setValue(attPos++, numFollowBacksMentions);
        inst.setValue(attPos++, numFollowTrickMentions);

        if (DEBUG) {
            System.out.println("-------");
            System.out.println("twitter header:");
            System.out.println(dataset);
            System.out.println("generated twitter instance:");
            System.out.println(inst);
            System.out.println("-------");
        }

        return inst;
    }

    @Override
    public ArrayList<IFact> obtainFactList() {
        return this.facts;
    }
}

