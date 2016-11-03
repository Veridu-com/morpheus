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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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

    private static final IFact numTweetsFact = new Fact("numOfTweets", "twitter");

    private static final IFact numRetweetsFact = new Fact("numOfRetweetedTweets", "twitter");

    private static final IFact numFriendsFact = new Fact("numOfFriends", "twitter");

    private static final IFact numOfFollowersFact = new Fact("numOfFollowers", "twitter");

    private static HashSet<String> automatedTools = new HashSet<>();

    private static final Pattern quoteBotsPattern = Pattern.compile("\\d+.(“|\")+(.+)(\"|”)+\\s*(-|–)+\\s*.+");

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

        IFact numOfRatioFollowTricks = new Fact("numOfRatioFollowTricks", "skynet");
        IFact numOfConsecutiveTweetsInASec = new Fact("numOfConsecutiveTweetsInASec", "skynet");
        IFact numOfFracRetweetOverTweet = new Fact("numOfFracRetweetOverTweet", "skynet");
        IFact numOfTweetsFromAutomated = new Fact("numOfTweetsFromAutomated", "skynet");
        IFact numOfFracQuoteBotsPatterns = new Fact("numOfFracQuoteBotsPatterns", "skynet");
        //IFact numOfRatioFollowersFollowing = new Fact("numOfRatioFollowersFollowing", "skynet");

        this.facts.add(numOfRatioFollowTricks);
        this.facts.add(numOfConsecutiveTweetsInASec);
        this.facts.add(numOfFracRetweetOverTweet);
        this.facts.add(numOfTweetsFromAutomated);
        this.facts.add(numOfFracQuoteBotsPatterns);
        //this.facts.add(numOfRatioFollowersFollowing);

        populateAutomatedTweetingTools();
    }

    private void populateAutomatedTweetingTools() {
        automatedTools.add("hootsuite");
        automatedTools.add("cotweet");
        automatedTools.add("socialoomph");
        automatedTools.add("twuffer");
        automatedTools.add("su.pr");
        automatedTools.add("twaitter");
        automatedTools.add("taweet");
        automatedTools.add("tweet-u-later");
        automatedTools.add("tweetfunnel");
        automatedTools.add("futuretweets");
    }

    private static Date parseTwitterUTC(String date) {

        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";

        // Important note. Only ENGLISH Locale works.
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            return sf.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * computes the time difference in seconds
     *
     * @param date1
     * @param date2
     * @return
     */
    private static long getDateDiff(Date date1, Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return Math.abs(TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS));
    }

    private static int countNumFollowsTricks(String txtMessage) {

        int numFollowTricks = 0;

        if (txtMessage.contains("retweet"))
            numFollowTricks++;
        if (txtMessage.contains("followtrain"))
            numFollowTricks++;
        if (txtMessage.contains("followers"))
            numFollowTricks++;
        if (txtMessage.contains("followback"))
            numFollowTricks++;
        if (txtMessage.contains("followtrick"))
            numFollowTricks++;
        if (txtMessage.contains("mgwv"))
            numFollowTricks++;
        if (txtMessage.contains("siguemeytesigo"))
            numFollowTricks++;
        if (txtMessage.contains("seguime"))
            numFollowTricks++;

        return numFollowTricks;
    }

    private boolean isFromAutomatedSource(String source) {
        for (String key : automatedTools)
            if (source.contains(key))
                return true;
        return false;
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

        int numFollowTricks = 0;
        int numOfConsecutiveTweetsInASec = 0;
        int numOfTweetsFromAutomated = 0;
        int numOfQuoteBotsPatterns = 0;

        Date lastDate = null;

        if (tweets != null) {
            for (int i = 0; i < tweets.size(); i++) {
                JsonObject tweet = tweets.get(i).getAsJsonObject();
                String txtMessage = tweet.get("text").getAsString().toLowerCase().replaceAll("\\s+", "");
                String rawText = tweet.get("text").getAsString();

                if (quoteBotsPattern.matcher(rawText).matches())
                    numOfQuoteBotsPatterns++;

                Date currDate = parseTwitterUTC(tweet.get("created_at").getAsString());
                String source = tweet.get("source").getAsString().toLowerCase();

                numFollowTricks += countNumFollowsTricks(txtMessage);

                if (lastDate != null) {
                    long diffInSeconds = getDateDiff(lastDate, currDate);
                    if (diffInSeconds <= 1)
                        numOfConsecutiveTweetsInASec++;
                }

                lastDate = currDate;

                if (isFromAutomatedSource(source))
                    numOfTweetsFromAutomated++;
            }
        }

        double numTweets = 0;
        double numRetweets = 0;
        double fracRetweetsOverTweets = 0;

        double numOfFollowers = 0;
        double numOfFriends = 0;

        double fracQuoteBotsPatterns = 0;
        double ratioFollowersFollowing = 0;

        double ratioFollowTricks = 0;

        if (numericFacts.containsKey(numTweetsFact))
            numTweets = numericFacts.get(numTweetsFact);

        if (numericFacts.containsKey(numRetweetsFact))
            numRetweets = numericFacts.get(numRetweetsFact);

        if (numTweets > 0)
            fracRetweetsOverTweets = numRetweets / numTweets;

        if (numericFacts.containsKey(numOfFollowersFact))
            numOfFollowers = numericFacts.get(numOfFollowersFact);

        if (numericFacts.containsKey(numFriendsFact))
            numOfFriends = numericFacts.get(numFriendsFact);

        if (numTweets > 0)
            fracQuoteBotsPatterns = numOfQuoteBotsPatterns / numTweets;

        if (numOfFriends > 0)
            ratioFollowersFollowing = numOfFollowers / numOfFriends;

        if (numTweets > 0)
            ratioFollowTricks = numFollowTricks / numTweets;

        inst.setValue(attPos++, ratioFollowTricks);
        inst.setValue(attPos++, numOfConsecutiveTweetsInASec);
        inst.setValue(attPos++, fracRetweetsOverTweets);
        inst.setValue(attPos++, numOfTweetsFromAutomated);
        inst.setValue(attPos++, fracQuoteBotsPatterns);
        //inst.setValue(attPos++, ratioFollowersFollowing);

        if (DEBUG) {
            System.out.println("-------------------");
            System.out.println("header:");
            System.out.println(dataset.toString());
            System.out.println("generated instance:");
            System.out.println(inst);
            System.out.println("-------------------");
        }

        return inst;
    }

    @Override
    public ArrayList<IFact> obtainFactList() {
        return this.facts;
    }
}

