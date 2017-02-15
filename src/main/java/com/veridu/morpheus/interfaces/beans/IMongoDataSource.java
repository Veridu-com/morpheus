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

public interface IMongoDataSource {

    Date getFacebookBirthday(IdOSAPIFactory factory, IUser user);

    int getNumberOfFacebookPostsOnBirthday(IdOSAPIFactory factory, IUser user, Date fbkBirthday);

    int getNumberOfFacebookTagsOnBirthday(IdOSAPIFactory factory, IUser user, Date fbkBirthday);

    int getNumberOfFacebookStatusOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday);

    int getNumberOfFacebookPhotosOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday);

    int getNumberOfFacebookLikesOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday);

    int getNumberOfFacebookEventsOnBirthday(IdOSAPIFactory factory, IUser user, Date facebookBirthday);

    JsonObject getFacebookProfile(IdOSAPIFactory factory, IUser user);

    Boolean doesFacebookFirstNameMatchEmail(IdOSAPIFactory factory, IUser user);

    int getNumberOfFacebookPosts(IdOSAPIFactory factory, IUser user);

    JsonArray getFacebookPosts(IdOSAPIFactory factory, IUser user);

    JsonObject getProviderArray(IdOSAPIFactory factory, IUser user, String provider, String collection);

    int getFacebookCollectionCount(IdOSAPIFactory factory, IUser user, String collection);

    JsonArray getFacebookFamily(IdOSAPIFactory factory, IUser user);

    JsonObject getAmazonProfile(IdOSAPIFactory factory, IUser user);

    JsonObject getDropboxProfile(IdOSAPIFactory factory, IUser user);

    JsonObject getGoogleProfile(IdOSAPIFactory factory, IUser user);

    JsonObject getLinkedinProfile(IdOSAPIFactory factory, IUser user);

    JsonObject getTwitterProfile(IdOSAPIFactory factory, IUser user);

    JsonObject getPaypalProfile(IdOSAPIFactory factory, IUser user);

    JsonArray getTweets(IdOSAPIFactory factory, IUser user);

    JsonArray getGmailMessages(IdOSAPIFactory factory, IUser user);
}
