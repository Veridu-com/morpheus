/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.beans;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.interfaces.users.IUser;

import java.util.Date;

/**
 * Defines access methods to data contained in the NoSQL database of idOS
 */
public interface IMongoDataSource {

    /**
     * Obtain facebook birthday from Mongo db
     *
     * @param factory idOS API factory
     * @param user the user to find the birthday for
     * @return birthday as a java Date object
     */
    Date getFacebookBirthday(IdOSAPIFactory factory, IUser user);

    /**
     * Get the number of facebook posts on the users birthday
     * @param factory idOS API factory
     * @param user selected user
     * @param fbkBirthday facebook date of birth as a java date object
     * @return the number of facebook posts on that date
     */
    int getNumberOfFacebookPostsOnBirthday(IdOSAPIFactory factory, IUser user, Date fbkBirthday);

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
    int getFacebookArrayCountBirthday(IdOSAPIFactory factory, IUser user, Date facebookBday, String collection,
            String timeTag);

    /**
     * Get the number of times the user was tagged on posts in his/her birthday
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param fbkBirthday users birthday
     * @return the number of tags
     */
    int getNumberOfFacebookTagsOnBirthday(IdOSAPIFactory factory, IUser user, Date fbkBirthday);

    /**
     * Get the number of facebook status updates on the users birthday
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param facebookBirthday users birthday
     * @return the number of status updates
     */
    int getNumberOfFacebookStatusOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday);

    /**
     * Get the number of facebook photos posted on the users birthday
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param facebookBirthday users birthday
     * @return the number of photos
     */
    int getNumberOfFacebookPhotosOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday);

    /**
     * Get the number of facebook status updates on the users birthday
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param facebookBirthday users birthday
     * @return the number of status updates
     */
    int getNumberOfFacebookLikesOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday);

    /**
     * Get the number of facebook events on the users birthday
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param facebookBirthday users birthday
     * @return the number of events
     */
    int getNumberOfFacebookEventsOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday);

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
    JsonObject getProviderCollection(IdOSAPIFactory factory, IUser user, String provider, String collection);

    /**
     * Get a collection data element as a json array
     *
     * @param factory idOS API factory
     * @param user the selected user
     * @param provider provider name
     * @param collection desired coleection, e.g., posts
     * @return collection data as a json array
     */
    JsonArray getProviderCollectionAsArray(IdOSAPIFactory factory, IUser user, String provider, String collection);

    /**
     * Get provider collection data field as a json object
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param provider provider name
     * @param collection provider collection
     * @return collection data field as a json object
     */
    JsonObject getProviderCollectionAsDocument(IdOSAPIFactory factory, IUser user, String provider, String collection);

    /**
     * Get facebook profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the facebook profile as a json object
     */
    JsonObject getFacebookProfile(IdOSAPIFactory factory, IUser user);

    /**
     * Verify if the declared facebook first name matches the user field in the e-mail address
     *
     * @param factory idOS API factory
     * @param user selected user
     * @return true if first name is in the user field of the e-mail
     */
    Boolean doesFacebookFirstNameMatchEmail(IdOSAPIFactory factory, IUser user);

    /**
     * Get the number of facebook posts
     *
     * @param factory idOS API factory
     * @param user selected user
     * @return the number of posts the user has
     */
    int getNumberOfFacebookPosts(IdOSAPIFactory factory, IUser user);

    /**
     * Get facebook posts
     *
     * @param factory idOS API factory
     * @param user selected user
     * @return the users facebook posts
     */
    JsonArray getFacebookPosts(IdOSAPIFactory factory, IUser user);

    /**
     * Get the latest collection of a provider as a json object
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param provider provider name
     * @param collection desired collection
     * @return the provider collection as a json object
     */
    JsonObject getProviderArray(IdOSAPIFactory factory, IUser user, String provider, String collection);

    /**
     * Count the number of elements in the data section of a facebook collection
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param collection facebook collection name
     * @return the length of the elements in the data section of the facebook collection
     */
    int getFacebookCollectionCount(IdOSAPIFactory factory, IUser user, String collection);

    /**
     * Get facebook family members as a json array
     *
     * @param factory idOS API factory
     * @param user selected user
     * @return family members as a json array
     */
    JsonArray getFacebookFamily(IdOSAPIFactory factory, IUser user);

    /**
     * Get amazon profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the amazon profile as a json object
     */
    JsonObject getAmazonProfile(IdOSAPIFactory factory, IUser user);

    /**
     * Get dropbox profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the dropbox profile as a json object
     */
    JsonObject getDropboxProfile(IdOSAPIFactory factory, IUser user);

    /**
     * Get google profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the google profile as a json object
     */
    JsonObject getGoogleProfile(IdOSAPIFactory factory, IUser user);

    /**
     * Get linkedin profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the linkedin profile as a json object
     */
    JsonObject getLinkedinProfile(IdOSAPIFactory factory, IUser user);

    /**
     * Get twitter profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the twitter profile as a json object
     */
    JsonObject getTwitterProfile(IdOSAPIFactory factory, IUser user);

    /**
     * Get paypal profile
     * @param factory idOS API factory
     * @param user selected user
     * @return the paypal profile as a json object
     */
    JsonObject getPaypalProfile(IdOSAPIFactory factory, IUser user);

    /**
     * Get tweets
     *
     * @param factory idOS API factory
     * @param user selected user
     * @return the users tweets
     */
    JsonArray getTweets(IdOSAPIFactory factory, IUser user);

    /**
     * Get gmail messages
     *
     * @param factory idOS API factory
     * @param user selected user
     * @return the users gmail messages
     */
    JsonArray getGmailMessages(IdOSAPIFactory factory, IUser user);
}
