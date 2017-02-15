/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.tasks.flags;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.NameMismatch;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IMongoDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
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
import java.util.StringTokenizer;

/**
 * Created by cassio on 10/4/16.
 */
@Component("flags-recent-changes")
public class RecentChangesFlagTask implements ITask {

    private IUtils utils;

    private IMongoDataSource mongo;

    private IDataSource dao;

    private static final Logger log = Logger.getLogger(RecentChangesFlagTask.class);

    // *********************************************************************
    // List of recent changes flags
    // *********************************************************************
    private static final String FLAG_RECENT_NAME_CHANGES = "recent-name-changes";
    private static final String FLAG_RECENT_NAME_CHANGES_FACEBOOK = "recent-name-changes-facebook";
    private static final String FLAG_RECENT_NAME_CHANGES_GOOGLE = "recent-name-changes-google";

    @Autowired
    public RecentChangesFlagTask(IUtils utils, IMongoDataSource mongo, IDataSource dao) {
        this.utils = utils;
        this.mongo = mongo;
        this.dao = dao;
    }

    @Async
    @Override
    public void runTask(@RequestBody Parameters params) {
        long time1 = System.currentTimeMillis(); // START CLOCK

        String userId = params.userName;
        String pubKey = params.publicKey;
        boolean verbose = params.verbose;

        IUser user = new User(userId);

        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(pubKey, userId));

        // delete all recent change warnings first
        this.dao.deleteFlag(factory, user, FLAG_RECENT_NAME_CHANGES);
        this.dao.deleteFlag(factory, user, FLAG_RECENT_NAME_CHANGES_FACEBOOK);
        this.dao.deleteFlag(factory, user, FLAG_RECENT_NAME_CHANGES_GOOGLE);

        JsonObject fbkProfile = this.mongo.getFacebookProfile(factory, user);
        JsonObject googleProfile = this.mongo.getGoogleProfile(factory, user);

        // name mismatches lists
        ArrayList<NameMismatch> fbkNameMismatches = new ArrayList<>();
        ArrayList<NameMismatch> googleNameMismatches = new ArrayList<>();

        // ************************************************************************************
        // Facebook processing
        // ************************************************************************************
        boolean facebookRecentChanges = false;

        if (fbkProfile != null) {

            String fbkProfileId = fbkProfile.get("id").getAsString();

            JsonArray posts = this.mongo.getFacebookPosts(factory, user);

            if (posts != null) {
                NormalizedLevenshtein leven = new NormalizedLevenshtein();
                for (int i = 0; i < posts.size(); i++) {
                    JsonObject post = posts.get(i).getAsJsonObject();
                    // search in the comments of a post for a tag of the original user
                    if (post.has("comments")) {
                        JsonArray data = post.get("comments").getAsJsonObject().get("data").getAsJsonArray();
                        for (int j = 0; j < data.size(); j++) {
                            JsonObject datum = data.get(j).getAsJsonObject();
                            if (datum.has("message_tags")) {
                                JsonArray tags = datum.get("message_tags").getAsJsonArray();
                                for (int k = 0; k < tags.size(); k++) {
                                    JsonObject tag = tags.get(k).getAsJsonObject();
                                    if (tag.get("id").getAsString().equals(fbkProfileId)) {
                                        // this is a tag of the profile owner
                                        // compare if the name within the body of the message is different from the
                                        // profile owner name
                                        String ownerName = tag.get("name").getAsString().toLowerCase();
                                        long offset = tag.get("offset").getAsLong();
                                        long length = tag.get("length").getAsLong();
                                        // System.err.println(String.format("going to check message with id %s",
                                        // datum.getString("id")));
                                        String messageBodyName = datum.get("message").getAsString()
                                                .substring((int) offset, (int) (offset + length)).toLowerCase();
                                        if (!ownerName.contains(messageBodyName) && (
                                                leven.similarity(ownerName, messageBodyName) < 0.8)) {
                                            NameMismatch nm = new NameMismatch(ownerName, messageBodyName,
                                                    datum.get("created_time").getAsString(),
                                                    datum.get("id").getAsString());
                                            fbkNameMismatches.add(nm);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (fbkNameMismatches.size() > 0) {
                        this.dao.insertFlag(factory, user, FLAG_RECENT_NAME_CHANGES_FACEBOOK,
                                Constants.PROFILE_PROVIDER_NAME);
                        facebookRecentChanges = true;
                    }
                }
            }
        }

        // check if any tags the user received on his posts has a name different then the profile name

        // ************************************************************************************
        // Google processing
        // ************************************************************************************
        boolean googleRecentChanges = false;

        if (googleProfile != null) {
            String googleProfileId = googleProfile.get("id").getAsString();

            JsonArray emails = this.mongo.getGmailMessages(factory, user);

            if (emails != null) {
                String givenName = googleProfile.get("given_name").getAsString();
                String familyName = googleProfile.get("family_name").getAsString();
                String declaredName = (givenName + " " + familyName).toLowerCase();
                String profileEmail = googleProfile.get("email").getAsString();

                NormalizedLevenshtein leven = new NormalizedLevenshtein();

                for (int i = 0; i < emails.size(); i++) {
                    JsonObject email = emails.get(i).getAsJsonObject();

                    // get the message headers
                    JsonArray headers = email.get("payload").getAsJsonObject().get("headers").getAsJsonArray();
                    for (int j = 0; j < headers.size(); j++) {
                        JsonObject head = headers.get(j).getAsJsonObject();

                        if (head.get("name").getAsString().equals("Delivered-To") && head.get("value").getAsString()
                                .equals(profileEmail)) // this is a received message

                            for (int k = 0; k < headers.size(); k++) {
                                JsonObject header = headers.get(k).getAsJsonObject();
                                String key = header.get("name").getAsString();
                                if (key.equals("To") && header.get("value").getAsString().contains(profileEmail)) {
                                    // now we have to search for our user inside the To: header, as multiple people
                                    // can be on the To: field.
                                    // TODO: we may have to search on CC and BCC as well
                                    String toValue = header.get("value").getAsString();
                                    // remove all quotations
                                    toValue = toValue.replace("'", "").replace("\"", "");
                                    String userToken = null;

                                    StringTokenizer st = new StringTokenizer(toValue, ",");
                                    while (st.hasMoreTokens()) {
                                        String token = st.nextToken();
                                        if (token.contains(profileEmail)) {
                                            userToken = token;
                                            break;
                                        }
                                    }

                                    if (userToken != null) {
                                        // use a regex to remove the email
                                        String citedName = userToken.replaceFirst("<([^>]+)>", "").trim();
                                        if (!citedName.equals(profileEmail) && citedName.length() > 0 && !declaredName
                                                .contains(citedName) && (leven.similarity(declaredName, citedName)
                                                < 0.8)) {// get the
                                            // date:
                                            String date = getProperty(headers, "Date");
                                            String msgId = getProperty(headers, "Message-ID");
                                            NameMismatch nm = new NameMismatch(declaredName, citedName, date, msgId);
                                            googleNameMismatches.add(nm);
                                            // System.err.println(String.format(
                                            // "To field => %s ; user token => %s ; cited name => %s",
                                            // header.getString("value"), userToken, citedName));
                                        }
                                    }
                                    break; // we've found our user
                                }
                            }
                    }
                }

                if (googleNameMismatches.size() > 0) {
                    this.dao.insertFlag(factory, user, FLAG_RECENT_NAME_CHANGES_GOOGLE,
                            Constants.PROFILE_PROVIDER_NAME);
                    googleRecentChanges = true;
                }
            }
        }

        // ************************************************************************************
        // End of processing, generate response
        // ************************************************************************************
        long time2, timediff = 0;

        String factValue = "0";

        if (facebookRecentChanges || googleRecentChanges) {
            this.dao.insertFlag(factory, user, FLAG_RECENT_NAME_CHANGES, Constants.PROFILE_PROVIDER_NAME);
            factValue = "1";
        }

        time2 = System.currentTimeMillis(); // STOP CLOCK
        timediff = time2 - time1;

        if (verbose)
            log.info(String.format("Recent changes flag returned %s for user %s in %d ms", factValue, userId,
                    time2 - time1));

        JsonObject responseBuilder = new JsonObject();

        responseBuilder.addProperty(Constants.MODEL_NAME_RESPONSE_STR, Constants.RECENT_CHANGES_FLAG);
        responseBuilder.addProperty(Constants.USER_ID_RESPONSE_STR, userId);
        responseBuilder.addProperty(Constants.FLAG_VALUE, factValue);
        responseBuilder.addProperty(Constants.TIME_TAKEN_RESPONSE_STR, timediff);

        JsonArray arBuilder = new JsonArray();

        if (facebookRecentChanges) {
            JsonObject obj = new JsonObject();
            obj.addProperty("provider", Constants.FACEBOOK_PROVIDER_NAME);
            JsonArray misAr = new JsonArray();
            for (NameMismatch nm : fbkNameMismatches) {
                JsonObject misMatch = new JsonObject();
                misMatch.addProperty("declaredName", nm.getDeclaredName());
                misMatch.addProperty("citedAsName", nm.getCitedAsName());
                String date = nm.getDate() != null ? nm.getDate() : "";
                misMatch.addProperty("date", date);
                String msgId = nm.getMsgId() != null ? nm.getMsgId() : "";
                misMatch.addProperty("msgId", msgId);
                misAr.add(misMatch);
            }
            obj.add("nameMismatches", misAr);
            arBuilder.add(obj);
        }

        if (googleRecentChanges) {
            JsonObject obj = new JsonObject();
            obj.addProperty("provider", Constants.GOOGLE_PROVIDER_NAME);
            JsonArray misAr = new JsonArray();
            for (NameMismatch nm : googleNameMismatches) {
                JsonObject misMatch = new JsonObject();
                misMatch.addProperty("declaredName", nm.getDeclaredName());
                misMatch.addProperty("citedAsName", nm.getCitedAsName());
                String date = nm.getDate() != null ? nm.getDate() : "";
                misMatch.addProperty("date", date);
                String msgId = nm.getMsgId() != null ? nm.getMsgId() : "";
                misMatch.addProperty("msgId", msgId);
                misAr.add(misMatch);
            }
            obj.add("nameMismatches", misAr);
            arBuilder.add(obj);
        }

        responseBuilder.add("recentChanges", arBuilder);

        if (params.verbose)
            System.out.println(responseBuilder.toString());

    }

    private String getProperty(JsonArray headers, String property) {
        for (int i = 0; i < headers.size(); i++) {
            JsonObject header = headers.get(i).getAsJsonObject();
            if (header.get("name").getAsString().equals(property))
                return header.get("value").getAsString();
        }
        return null;
    }
}
