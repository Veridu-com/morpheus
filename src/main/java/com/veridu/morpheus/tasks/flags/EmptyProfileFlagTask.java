package com.veridu.morpheus.tasks.flags;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.Parameters;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;

/**
 * Created by cassio on 10/4/16.
 */
@Component("flags-empty")
public class EmptyProfileFlagTask implements ITask {

    private IUtils utils;

    private IDataSource dao;

    private static final String FLAG_NAME = "compromised-email";

    private static final Logger log = Logger.getLogger(EmptyProfileFlagTask.class);

    private static final String myProvider = "skynet";

    private static final String prefixFactName = "flagEmptyProfile";

    // *********************************************************************
    // List of warnings names
    // *********************************************************************
    private static final String FLAG_EMPTY_DROPBOX = "dropbox-empty";
    private static final String FLAG_EMPTY_FACEBOOK = "facebook-empty";
    private static final String FLAG_EMPTY_GOOGLE = "google-empty";
    private static final String FLAG_EMPTY_LINKEDIN = "linkedin-empty";
    private static final String FLAG_EMPTY_SPOTIFY = "spotify-empty";
    private static final String FLAG_EMPTY_TWITTER = "twitter-empty";
    private static final String FLAG_EMPTY_YAHOO = "yahoo-empty";
    private static final String FLAG_EMPTY_ACCOUNT = "account-empty";

    // *********************************************************************
    // List of provider facts
    // *********************************************************************

    // dropbox facts
    private static final IFact dropboxFirstName = new Fact("firstName", Constants.DROPBOX_PROVIDER_NAME);
    private static final IFact dropboxLastName = new Fact("lastName", Constants.DROPBOX_PROVIDER_NAME);
    private static final IFact dropboxNumFiles = new Fact("numFiles", Constants.DROPBOX_PROVIDER_NAME);

    // facebook facts
    private static final IFact facebookFirstName = new Fact("firstName", Constants.FACEBOOK_PROVIDER_NAME);
    private static final IFact facebookLastName = new Fact("lastName", Constants.FACEBOOK_PROVIDER_NAME);
    private static final IFact facebookNumOfLikes = new Fact("numOfLikes", Constants.FACEBOOK_PROVIDER_NAME);
    private static final IFact facebookNumOfPosts = new Fact("numOfPosts", Constants.FACEBOOK_PROVIDER_NAME);
    private static final IFact facebookNumOfReceivedComments = new Fact("numOfReceivedComments",
            Constants.FACEBOOK_PROVIDER_NAME);
    private static final IFact facebookNumOfReceivedLikes = new Fact("numOfReceivedLikes",
            Constants.FACEBOOK_PROVIDER_NAME);
    private static final IFact facebookNumOfStatuses = new Fact("numOfStatuses", Constants.FACEBOOK_PROVIDER_NAME);

    // google facts
    private static final IFact googleNumGmailContacts = new Fact("numGmailContacts", Constants.GOOGLE_PROVIDER_NAME);
    private static final IFact googleNumGmailMessages = new Fact("numGmailMessages", Constants.GOOGLE_PROVIDER_NAME);
    private static final IFact googleNumGmailSentMessages = new Fact("numGmailSentMessages",
            Constants.GOOGLE_PROVIDER_NAME);

    // linkedin facts
    private static final IFact linkedinNumOfCompanies = new Fact("numOfCompanies", Constants.LINKEDIN_PROVIDER_NAME);
    private static final IFact linkedinNumOfConnections = new Fact("numOfConnections",
            Constants.LINKEDIN_PROVIDER_NAME);
    private static final IFact linkedinNumOfPositions = new Fact("numOfPositions", Constants.LINKEDIN_PROVIDER_NAME);

    // spotify facts
    private static final IFact spotifyNumPlaylists = new Fact("numPlaylists", Constants.SPOTIFY_PROVIDER_NAME);
    private static final IFact spotifyNumUniqueTracks = new Fact("numUniqueTracks", Constants.SPOTIFY_PROVIDER_NAME);

    // twitter facts
    private static final IFact twitterNumOfFavoritedTweets = new Fact("numOfFavoritedTweets",
            Constants.TWITTER_PROVIDER_NAME);
    private static final IFact twitterNumOfFollowers = new Fact("numOfFollowers", Constants.TWITTER_PROVIDER_NAME);
    private static final IFact twitterNumOfFriends = new Fact("numOfFriends", Constants.TWITTER_PROVIDER_NAME);
    private static final IFact twitterNumOfTweets = new Fact("numOfTweets", Constants.TWITTER_PROVIDER_NAME);

    // yahoo facts
    private static final IFact yahooPostalCode = new Fact("postalCode", Constants.YAHOO_PROVIDER_NAME);
    private static final IFact yahooStreetAddress = new Fact("streetAddress", Constants.YAHOO_PROVIDER_NAME);
    private static final IFact yahooCountryName = new Fact("countryName", Constants.YAHOO_PROVIDER_NAME);
    private static final IFact yahooCityName = new Fact("cityName", Constants.YAHOO_PROVIDER_NAME);
    private static final IFact yahooNumContacts = new Fact("numContacts", Constants.YAHOO_PROVIDER_NAME);

    @Autowired
    public EmptyProfileFlagTask(IUtils utils, IDataSource dao) {
        this.utils = utils;
        this.dao = dao;
    }

    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {
        long time1 = System.currentTimeMillis(); // START CLOCK

        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

        IUser user = new User(userId);

        // ************************************************************************************
        // Delete all warnings first
        // ************************************************************************************
        this.dao.deleteWarning(factory, user, FLAG_EMPTY_ACCOUNT);
        this.dao.deleteWarning(factory, user, FLAG_EMPTY_DROPBOX);
        this.dao.deleteWarning(factory, user, FLAG_EMPTY_FACEBOOK);
        this.dao.deleteWarning(factory, user, FLAG_EMPTY_GOOGLE);
        this.dao.deleteWarning(factory, user, FLAG_EMPTY_LINKEDIN);
        this.dao.deleteWarning(factory, user, FLAG_EMPTY_SPOTIFY);
        this.dao.deleteWarning(factory, user, FLAG_EMPTY_TWITTER);
        this.dao.deleteWarning(factory, user, FLAG_EMPTY_YAHOO);

        // ************************************************************************************
        // Dropbox processing
        // ************************************************************************************
        boolean dropboxEmpty = false;

        HashMap<IFact, String> dropboxFacts = this.dao
                .obtainProviderFactsForUser(factory, user, Constants.DROPBOX_PROVIDER_NAME);

        if (dropboxFacts.size() > 0) // the user has something on dropbox
            if (dropboxFacts.containsKey(dropboxFirstName) && dropboxFacts.containsKey(dropboxLastName) && dropboxFacts
                    .containsKey(dropboxNumFiles)) {
                String fName = dropboxFacts.get(dropboxFirstName);
                String lName = dropboxFacts.get(dropboxLastName);
                int nFiles = Integer.parseInt(dropboxFacts.get(dropboxNumFiles));
                if (fName.equals("") || lName.equals("") || (nFiles < 10))
                    dropboxEmpty = true;
            } else
                dropboxEmpty = true;

        if (dropboxEmpty)
            this.dao.insertWarning(factory, user, FLAG_EMPTY_DROPBOX, Constants.PROFILE_PROVIDER_NAME);

        // ************************************************************************************
        // Facebook processing
        // ************************************************************************************
        boolean facebookEmpty = false;

        HashMap<IFact, String> facebookFacts = this.dao
                .obtainProviderFactsForUser(factory, user, Constants.FACEBOOK_PROVIDER_NAME);

        if (facebookFacts.size() > 0) // user has something on facebook
            if (facebookFacts.containsKey(facebookFirstName) && facebookFacts.containsKey(facebookLastName)
                    && facebookFacts.containsKey(facebookNumOfLikes) && facebookFacts.containsKey(facebookNumOfPosts)
                    && facebookFacts.containsKey(facebookNumOfReceivedComments) && facebookFacts
                    .containsKey(facebookNumOfReceivedLikes) && facebookFacts.containsKey(facebookNumOfStatuses)) {

                String fName = facebookFacts.get(facebookFirstName);
                String lName = facebookFacts.get(facebookLastName);
                int nLikes = Integer.parseInt(facebookFacts.get(facebookNumOfLikes));
                int nPosts = Integer.parseInt(facebookFacts.get(facebookNumOfPosts));
                int nComments = Integer.parseInt(facebookFacts.get(facebookNumOfReceivedComments));
                int nReceivedLikes = Integer.parseInt(facebookFacts.get(facebookNumOfReceivedLikes));
                int nStatuses = Integer.parseInt(facebookFacts.get(facebookNumOfStatuses));

                if (fName.equals("") || lName.equals("") || (nLikes < 14) || (nPosts < 18) || (nComments < 6) || (
                        nReceivedLikes < 16) || (nStatuses < 3))
                    facebookEmpty = true;
            } else
                facebookEmpty = true;

        if (facebookEmpty)
            this.dao.insertWarning(factory, user, FLAG_EMPTY_FACEBOOK, Constants.PROFILE_PROVIDER_NAME);

        // ************************************************************************************
        // Google processing
        // ************************************************************************************
        boolean googleEmpty = false;

        HashMap<IFact, String> googleFacts = this.dao
                .obtainProviderFactsForUser(factory, user, Constants.GOOGLE_PROVIDER_NAME);

        if (googleFacts.size() > 0) // user has something on google
            if (googleFacts.containsKey(googleNumGmailContacts) && googleFacts.containsKey(googleNumGmailMessages)
                    && googleFacts.containsKey(googleNumGmailSentMessages)) {

                int nContacts = Integer.parseInt(googleFacts.get(googleNumGmailContacts));
                int nReceivedMes = Integer.parseInt(googleFacts.get(googleNumGmailMessages));
                int nSentMes = Integer.parseInt(googleFacts.get(googleNumGmailSentMessages));

                if ((nContacts < 8) || (nReceivedMes < 17) || (nSentMes < 7))
                    googleEmpty = true;
            } else
                googleEmpty = true;

        if (googleEmpty)
            this.dao.insertWarning(factory, user, FLAG_EMPTY_GOOGLE, Constants.PROFILE_PROVIDER_NAME);

        // ************************************************************************************
        // Linkedin processing
        // ************************************************************************************
        boolean linkedinEmpty = false;

        HashMap<IFact, String> linkedinFacts = this.dao
                .obtainProviderFactsForUser(factory, user, Constants.LINKEDIN_PROVIDER_NAME);

        if (linkedinFacts.size() > 0) // user has something on google
            if (linkedinFacts.containsKey(linkedinNumOfCompanies) && linkedinFacts.containsKey(linkedinNumOfConnections)
                    && linkedinFacts.containsKey(linkedinNumOfPositions)) {

                int nCompanies = Integer.parseInt(linkedinFacts.get(linkedinNumOfCompanies));
                int nConnections = Integer.parseInt(linkedinFacts.get(linkedinNumOfConnections));
                int nPositions = Integer.parseInt(linkedinFacts.get(linkedinNumOfPositions));

                if ((nCompanies < 1) || (nConnections < 7) || (nPositions < 1))
                    linkedinEmpty = true;
            } else
                linkedinEmpty = true;

        if (linkedinEmpty)
            this.dao.insertWarning(factory, user, FLAG_EMPTY_LINKEDIN, Constants.PROFILE_PROVIDER_NAME);

        // ************************************************************************************
        // Spotify processing
        // ************************************************************************************
        boolean spotifyEmpty = false;

        HashMap<IFact, String> spotifyFacts = this.dao
                .obtainProviderFactsForUser(factory, user, Constants.SPOTIFY_PROVIDER_NAME);

        if (spotifyFacts.size() > 0) // user has something on google
            if (spotifyFacts.containsKey(spotifyNumPlaylists) && spotifyFacts.containsKey(spotifyNumUniqueTracks)) {

                int nPlaylists = Integer.parseInt(spotifyFacts.get(spotifyNumPlaylists));
                int nTracks = Integer.parseInt(spotifyFacts.get(spotifyNumUniqueTracks));

                if ((nPlaylists < 2) || (nTracks < 20))
                    spotifyEmpty = true;
            } else
                spotifyEmpty = true;

        if (spotifyEmpty)
            this.dao.insertWarning(factory, user, FLAG_EMPTY_SPOTIFY, Constants.PROFILE_PROVIDER_NAME);

        // ************************************************************************************
        // Twitter processing
        // ************************************************************************************
        boolean twitterEmpty = false;

        HashMap<IFact, String> twitterFacts = this.dao
                .obtainProviderFactsForUser(factory, user, Constants.TWITTER_PROVIDER_NAME);

        if (twitterFacts.size() > 0) // user has something on google
            if (twitterFacts.containsKey(twitterNumOfFavoritedTweets) && twitterFacts.containsKey(twitterNumOfFollowers)
                    && twitterFacts.containsKey(twitterNumOfFriends) && twitterFacts.containsKey(twitterNumOfTweets)) {

                int nFavorited = Integer.parseInt(twitterFacts.get(twitterNumOfFavoritedTweets));
                int nFollowers = Integer.parseInt(twitterFacts.get(twitterNumOfFollowers));
                int nFriends = Integer.parseInt(twitterFacts.get(twitterNumOfFriends));
                int nTweets = Integer.parseInt(twitterFacts.get(twitterNumOfTweets));

                if ((nFavorited < 10) || (nFollowers < 5) || (nFriends < 12) || (nTweets < 5))
                    twitterEmpty = true;
            } else
                twitterEmpty = true;

        if (twitterEmpty)
            this.dao.insertWarning(factory, user, FLAG_EMPTY_TWITTER, Constants.PROFILE_PROVIDER_NAME);

        // ************************************************************************************
        // Yahoo processing
        // ************************************************************************************
        boolean yahooEmpty = false;

        HashMap<IFact, String> yahooFacts = this.dao
                .obtainProviderFactsForUser(factory, user, Constants.YAHOO_PROVIDER_NAME);

        if (yahooFacts.size() > 0) // user has something on google
            if (yahooFacts.containsKey(yahooPostalCode) && yahooFacts.containsKey(yahooStreetAddress) && yahooFacts
                    .containsKey(yahooCountryName) && yahooFacts.containsKey(yahooCityName) && yahooFacts
                    .containsKey(yahooNumContacts)) {

                String postalCode = twitterFacts.get(yahooPostalCode);
                String streetAdd = twitterFacts.get(yahooStreetAddress);
                String countryName = twitterFacts.get(yahooCountryName);
                String cityName = twitterFacts.get(yahooCityName);
                int numContacts = Integer.parseInt(twitterFacts.get(yahooNumContacts));

                if ((postalCode.equals("")) || (streetAdd.equals("")) || (countryName.equals("")) || (cityName
                        .equals("")) || (numContacts < 5))
                    yahooEmpty = true;
            } else
                yahooEmpty = true;

        if (yahooEmpty)
            this.dao.insertWarning(factory, user, FLAG_EMPTY_YAHOO, Constants.PROFILE_PROVIDER_NAME);

        // ************************************************************************************
        // End of provider facts processing
        // ************************************************************************************
        long time2, timediff = 0;

        time2 = System.currentTimeMillis(); // STOP CLOCK
        timediff = time2 - time1;
        String factValue = "0";

        if (dropboxEmpty || facebookEmpty || googleEmpty || linkedinEmpty || spotifyEmpty || twitterEmpty
                || yahooEmpty) {
            this.dao.insertWarning(factory, user, FLAG_EMPTY_ACCOUNT, Constants.PROFILE_PROVIDER_NAME);
            factValue = "1";
        }

        if (verbose)
            log.info(String.format("Empty flag returned %s for user %s in %d ms", factValue, userId, time2 - time1));

        JsonObject responseBuilder = new JsonObject();

        responseBuilder.addProperty(Constants.MODEL_NAME_RESPONSE_STR, Constants.EMPTY_FLAG);
        responseBuilder.addProperty(Constants.USER_ID_RESPONSE_STR, userId);
        responseBuilder.addProperty(Constants.FLAG_VALUE, factValue);
        responseBuilder.addProperty(Constants.TIME_TAKEN_RESPONSE_STR, timediff);

        JsonArray arBuilder = new JsonArray();

        if (dropboxEmpty)
            arBuilder.add(Constants.DROPBOX_PROVIDER_NAME);

        if (facebookEmpty)
            arBuilder.add(Constants.FACEBOOK_PROVIDER_NAME);

        if (googleEmpty)
            arBuilder.add(Constants.GOOGLE_PROVIDER_NAME);

        if (linkedinEmpty)
            arBuilder.add(Constants.LINKEDIN_PROVIDER_NAME);

        if (spotifyEmpty)
            arBuilder.add(Constants.SPOTIFY_PROVIDER_NAME);

        if (twitterEmpty)
            arBuilder.add(Constants.TWITTER_PROVIDER_NAME);

        if (yahooEmpty)
            arBuilder.add(Constants.YAHOO_PROVIDER_NAME);

        responseBuilder.add("emptyProfilesList", arBuilder);

        if (params.verbose)
            System.out.println(responseBuilder);
    }

}
