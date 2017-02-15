/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.dataaccess;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.idos.exceptions.SDKException;
import com.veridu.idos.utils.Filter;
import com.veridu.idos.utils.IdOSAuthType;
import com.veridu.morpheus.interfaces.beans.IMongoDataSource;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.LocalUtils;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by cassio on 10/3/16.
 */
@Component("idosNOSQL")
public class NOSQLIDOSAccess implements IMongoDataSource {

    @Override
    public Date getFacebookBirthday(IdOSAPIFactory factory, IUser user) {
        JsonObject profile = this.getFacebookProfile(factory, user);
        if (LocalUtils.validateJsonField(profile, "birthday"))
            return LocalUtils.parseFacebookDate(profile.get("birthday").getAsString());
        return null;
    }

    private JsonObject getProviderCollection(IdOSAPIFactory factory, IUser user, String provider, String collection) {

        try {
            factory.getRaw().setAuthType(IdOSAuthType.HANDLER);

            return factory.getRaw().listAll(user.getId(),
                    Filter.createFilter().addRawSourceNameFilter(provider).addCollectionFilter(collection)
                            .addRawFilterOrderLatest().addLimitFilter(1));

        } catch (SDKException e) {
            e.printStackTrace();
        }

        return null;
    }

    private JsonElement getResponseDataContent(JsonObject response) {
        JsonArray jobj = LocalUtils.getResponseData(response);
        if (jobj != null && !jobj.isJsonNull() && jobj.size() > 0 && !jobj.get(0).getAsJsonObject().get("data")
                .isJsonNull())
            return jobj.get(0).getAsJsonObject().get("data");
        return null;
    }

    private JsonArray getProviderCollectionAsArray(IdOSAPIFactory factory, IUser user, String provider,
            String collection) {

        JsonObject response = getProviderCollection(factory, user, provider, collection);

        if ((response != null) && LocalUtils.okResponse(response)) {
            JsonElement elem = getResponseDataContent(response);
            if (elem != null && elem.isJsonArray())
                return elem.getAsJsonArray();
        }

        return null;

    }

    private JsonObject getProviderCollectionAsDocument(IdOSAPIFactory factory, IUser user, String provider,
            String collection) {

        JsonObject response = getProviderCollection(factory, user, provider, collection);

        if ((response != null) && LocalUtils.okResponse(response)) {
            JsonElement elem = getResponseDataContent(response);
            if (elem != null && elem.isJsonObject())
                return elem.getAsJsonObject();
        }

        return null;
    }

    @Override
    public JsonObject getFacebookProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "facebook", "profile");
    }

    @Override
    public Boolean doesFacebookFirstNameMatchEmail(IdOSAPIFactory factory, IUser user) {
        JsonObject facebookProfile = getFacebookProfile(factory, user);
        if (LocalUtils.validateJsonField(facebookProfile, "email") && LocalUtils
                .validateJsonField(facebookProfile, "first_name")) {
            String email = facebookProfile.get("email").getAsString();
            String firstName = facebookProfile.get("first_name").getAsString();
            if ((email != null) && (firstName != null)) {
                String normalizedName = LocalUtils.normalizeName(firstName);
                return email.contains(normalizedName);
            }
        }
        return null;
    }

    @Override
    public JsonObject getAmazonProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "amazon", "profile");
    }

    @Override
    public JsonObject getDropboxProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "dropbox", "profile");
    }

    @Override
    public JsonObject getGoogleProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "google", "profile");
    }

    @Override
    public JsonObject getLinkedinProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "linkedin", "profile");
    }

    @Override
    public JsonObject getTwitterProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "twitter", "profile");
    }

    @Override
    public JsonObject getPaypalProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "paypal", "profile");
    }

    @Override
    public int getNumberOfFacebookPostsOnBirthday(IdOSAPIFactory factory, IUser user, Date fbkBirthday) {
        return getFacebookArrayCountBirthday(factory, user, fbkBirthday, "posts", "created_time");
    }

    private int getFacebookArrayCountBirthday(IdOSAPIFactory factory, IUser user, Date facebookBday, String collection,
            String timeTag) {

        int count = 0;

        if (facebookBday != null) {
            JsonArray arr = this.getProviderCollectionAsArray(factory, user, "facebook", collection);
            if (arr != null)
                for (int i = 0; i < arr.size(); i++) {
                    JsonObject jobj = arr.get(i).getAsJsonObject();
                    if (LocalUtils.validateJsonField(jobj, timeTag)) {
                        String createdTime = jobj.get(timeTag).getAsString();
                        if (createdTime != null) {
                            Date postDate = LocalUtils.parseFacebookRFC822Date(createdTime);
                            if ((postDate != null) && LocalUtils.checkDayMonthMatch(facebookBday, postDate)) {
                                count++;
                            }

                        }
                    }
                }
        }

        return count;
    }

    @Override
    public int getNumberOfFacebookTagsOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday) {
        return getFacebookArrayCountBirthday(factory, user, facebookBirthday, "tagged", "created_time");
    }

    @Override
    public int getNumberOfFacebookStatusOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday) {
        return getFacebookArrayCountBirthday(factory, user, facebookBirthday, "statuses", "updated_time");
    }

    @Override
    public int getNumberOfFacebookPhotosOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday) {
        return getFacebookArrayCountBirthday(factory, user, facebookBirthday, "photos", "created_time");
    }

    @Override
    public int getNumberOfFacebookLikesOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday) {
        return getFacebookArrayCountBirthday(factory, user, facebookBirthday, "likes", "created_time");
    }

    @Override
    public int getNumberOfFacebookEventsOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday) {
        return getFacebookArrayCountBirthday(factory, user, facebookBirthday, "events", "start_time");
    }

    @Override
    public int getNumberOfFacebookPosts(IdOSAPIFactory factory, IUser user) {
        return getFacebookCollectionCount(factory, user, "posts");
    }

    @Override
    public JsonObject getProviderArray(IdOSAPIFactory factory, IUser user, String provider, String collection) {
        try {
            factory.getRaw().setAuthType(IdOSAuthType.HANDLER);
            return factory.getRaw().listAll(user.getId(),
                    Filter.createFilter().addRawSourceNameFilter(provider).addCollectionFilter(collection)
                            .addRawFilterOrderLatest().addLimitFilter(1));
        } catch (SDKException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getFacebookCollectionCount(IdOSAPIFactory factory, IUser user, String collection) {
        JsonObject response = getProviderArray(factory, user, "facebook", collection);

        if (LocalUtils.okResponse(response)) {
            JsonElement elem = getResponseDataContent(response);
            if (LocalUtils.validateJsonElement(elem) && elem.isJsonArray())
                return elem.getAsJsonArray().size();
        }

        return 0;
    }

    @Override
    public JsonArray getFacebookFamily(IdOSAPIFactory factory, IUser user) {
        return this.getProviderCollectionAsArray(factory, user, "facebook", "family");
    }

    @Override
    public JsonArray getFacebookPosts(IdOSAPIFactory factory, IUser user) {
        return this.getProviderCollectionAsArray(factory, user, "facebook", "posts");
    }

    @Override
    public JsonArray getTweets(IdOSAPIFactory factory, IUser user) {
        return this.getProviderCollectionAsArray(factory, user, "twitter", "statuses");
    }

    @Override
    public JsonArray getGmailMessages(IdOSAPIFactory factory, IUser user) {
        return this.getProviderCollectionAsArray(factory, user, "google", "messages");
    }

}

