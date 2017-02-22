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

    /**
     * Obtain facebook birthday from Mongo db
     *
     * @param factory idOS API factory
     * @param user the user to find the birthday for
     * @return birthday as a java Date object
     */
    @Override
    public Date getFacebookBirthday(IdOSAPIFactory factory, IUser user) {
        JsonObject profile = this.getFacebookProfile(factory, user);
        if (LocalUtils.validateJsonField(profile, "birthday"))
            return LocalUtils.parseFacebookDate(profile.get("birthday").getAsString());
        return null;
    }

    /**
     * Get a specific collection for a specific provider of a user. The most recent collection of that
     * provider is returned.
     *
     * @param factory idOS API factory
     * @param user the user to find the collection for
     * @param provider selected provider for the collection
     * @param collection desired collection
     * @return the most recent collection for that provider
     */
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

    /**
     * Get the content of an idOS json response
     *
     * @param response json object with idOS response
     * @return the json element containing only the data portion of the response
     */
    private JsonElement getResponseDataContent(JsonObject response) {
        JsonArray jobj = LocalUtils.getResponseData(response);
        if (jobj != null && !jobj.isJsonNull() && jobj.size() > 0 && !jobj.get(0).getAsJsonObject().get("data")
                .isJsonNull())
            return jobj.get(0).getAsJsonObject().get("data");
        return null;
    }

    /**
     * Get a collection data element as a json array
     *
     * @param factory idOS API factory
     * @param user the selected user
     * @param provider provider name
     * @param collection desired coleection, e.g., posts
     * @return collection data as a json array
     */
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

    /**
     * Get provider collection data field as a json object
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param provider provider name
     * @param collection provider collection
     * @return collection data field as a json object
     */
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

    /**
     * Get facebook profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the facebook profile as a json object
     */
    @Override
    public JsonObject getFacebookProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "facebook", "profile");
    }

    /**
     * Verify if the declared facebook first name matches the user field in the e-mail address
     *
     * @param factory idOS API factory
     * @param user selected user
     * @return true if first name is in the user field of the e-mail
     */
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

    /**
     * Get amazon profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the amazon profile as a json object
     */
    @Override
    public JsonObject getAmazonProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "amazon", "profile");
    }

    /**
     * Get dropbox profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the dropbox profile as a json object
     */
    @Override
    public JsonObject getDropboxProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "dropbox", "profile");
    }

    /**
     * Get google profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the google profile as a json object
     */
    @Override
    public JsonObject getGoogleProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "google", "profile");
    }

    /**
     * Get linkedin profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the linkedin profile as a json object
     */
    @Override
    public JsonObject getLinkedinProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "linkedin", "profile");
    }

    /**
     * Get twitter profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the twitter profile as a json object
     */
    @Override
    public JsonObject getTwitterProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "twitter", "profile");
    }

    /**
     * Get paypal profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the paypal profile as a json object
     */
    @Override
    public JsonObject getPaypalProfile(IdOSAPIFactory factory, IUser user) {
        return getProviderCollectionAsDocument(factory, user, "paypal", "profile");
    }

    /**
     * Get the number of facebook posts on the users birthday
     * @param factory idOS API factory
     * @param user selected user
     * @param fbkBirthday facebook date of birth as a java date object
     * @return the number of facebook posts on that date
     */
    @Override
    public int getNumberOfFacebookPostsOnBirthday(IdOSAPIFactory factory, IUser user, Date fbkBirthday) {
        return getFacebookArrayCountBirthday(factory, user, fbkBirthday, "posts", "created_time");
    }

    /**
     * Get the length of a facebook data array in a collection in the users birthday.
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param facebookBday the users birthday on facebook
     * @param collection the desired collection to extract the data field
     * @param timeTag name of the time field, e.g., created_at or updated_at
     * @return the array length
     */
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

    /**
     * Get the number of times the user was tagged on posts in his/her birthday
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param facebookBirthday users birthday
     * @return the number of tags
     */
    @Override
    public int getNumberOfFacebookTagsOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday) {
        return getFacebookArrayCountBirthday(factory, user, facebookBirthday, "tagged", "created_time");
    }

    /**
     * Get the number of facebook status updates on the users birthday
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param facebookBirthday users birthday
     * @return the number of status updates
     */
    @Override
    public int getNumberOfFacebookStatusOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday) {
        return getFacebookArrayCountBirthday(factory, user, facebookBirthday, "statuses", "updated_time");
    }

    /**
     * Get the number of facebook photos posted on the users birthday
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param facebookBirthday users birthday
     * @return the number of photos
     */
    @Override
    public int getNumberOfFacebookPhotosOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday) {
        return getFacebookArrayCountBirthday(factory, user, facebookBirthday, "photos", "created_time");
    }

    /**
     * Get the number of facebook status updates on the users birthday
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param facebookBirthday users birthday
     * @return the number of status updates
     */
    @Override
    public int getNumberOfFacebookLikesOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday) {
        return getFacebookArrayCountBirthday(factory, user, facebookBirthday, "likes", "created_time");
    }

    /**
     * Get the number of facebook events on the users birthday
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param facebookBirthday users birthday
     * @return the number of events
     */
    @Override
    public int getNumberOfFacebookEventsOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday) {
        return getFacebookArrayCountBirthday(factory, user, facebookBirthday, "events", "start_time");
    }

    /**
     * Get the number of facebook posts
     *
     * @param factory idOS API factory
     * @param user selected user
     * @return the number of posts the user has
     */
    @Override
    public int getNumberOfFacebookPosts(IdOSAPIFactory factory, IUser user) {
        return getFacebookCollectionCount(factory, user, "posts");
    }

    /**
     * Get the latest collection of a provider as a json object
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param provider provider name
     * @param collection desired collection
     * @return the provider collection as a json object
     */
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

    /**
     * Count the number of elements in the data section of a facebook collection
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param collection facebook collection name
     * @return the length of the elements in the data section of the facebook collection
     */
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

    /**
     * Get facebook family members as a json array
     *
     * @param factory idOS API factory
     * @param user selected user
     * @return family members as a json array
     */
    @Override
    public JsonArray getFacebookFamily(IdOSAPIFactory factory, IUser user) {
        return this.getProviderCollectionAsArray(factory, user, "facebook", "family");
    }

    /**
     * Get facebook posts
     *
     * @param factory idOS API factory
     * @param user selected user
     * @return the users facebook posts
     */
    @Override
    public JsonArray getFacebookPosts(IdOSAPIFactory factory, IUser user) {
        return this.getProviderCollectionAsArray(factory, user, "facebook", "posts");
    }

    /**
     * Get tweets
     *
     * @param factory idOS API factory
     * @param user selected user
     * @return the users tweets
     */
    @Override
    public JsonArray getTweets(IdOSAPIFactory factory, IUser user) {
        return this.getProviderCollectionAsArray(factory, user, "twitter", "statuses");
    }

    /**
     * Get gmail messages
     *
     * @param factory idOS API factory
     * @param user selected user
     * @return the users gmail messages
     */
    @Override
    public JsonArray getGmailMessages(IdOSAPIFactory factory, IUser user) {
        return this.getProviderCollectionAsArray(factory, user, "google", "messages");
    }

}
