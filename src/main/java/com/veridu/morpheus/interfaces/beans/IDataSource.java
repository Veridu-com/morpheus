/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.beans;

import com.google.gson.JsonElement;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.interfaces.facts.ICandidate;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.users.IProfile;
import com.veridu.morpheus.interfaces.users.IUser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Interface all data sources should follow. By programming to this interface, it should be relatively easy to change
 * data source from PostgreSQL to MySQL, to API calls, etc.
 *
 * @author cassio
 *
 */
public interface IDataSource {

    /**
     * Insert a flag for a given user
     *
     * @param factory idOS API factory
     * @param user given user
     * @param flagName the flag name
     * @param attribute the attribute name
     */
    void insertFlag(IdOSAPIFactory factory, IUser user, String flagName, String attribute);

    /**
     * Delete a flag for a given user
     *
     * @param factory idOS API factory
     * @param user given user
     * @param flagName flag name to delete
     *
     */
    void deleteFlag(IdOSAPIFactory factory, IUser user, String flagName);

    /**
     * Insert attribute candidates for a user and a given attribute name
     *
     * @param factory idOS API factory
     * @param user the given user
     * @param attName attribute name
     * @param candidates the candidate values to insert in a list
     * @return 1 if the operation succeeded
     */
    int insertAttributeCandidatesForUser(IdOSAPIFactory factory, IUser user, String attName,
            ArrayList<ICandidate> candidates);

    /**
     * Obtain the latest source id for a provider
     * @param factory idOS API factory
     * @param userId the given user id
     * @param provider a provider name
     * @return the source id that has been created at the latest time
     */
    int getLatestSourceIdForProvider(IdOSAPIFactory factory, String userId, String provider);

    /**
     * Obtain the facts for a given user and provider. The return is a map of Facts.
     *
     * @param factory
     *             idOS API factory
     * @param user
     *            will use user_id as profile_id for query
     * @param provider
     *            desired provider of facts
     * @return set
     */
    HashMap<IFact, String> obtainProviderFactsForUser(IdOSAPIFactory factory, IUser user, String provider);

    /**
     * Deletes all facts from a specific provider for a specific user.
     *
     * @param factory
     *             idOS API factory
     * @param user
     *            will use the userId as profileId in the facts table
     * @param provider
     *            the provider for whom the facts should be wiped
     * @return number of deleted rows
     */
    int deleteProviderFactsForUser(IdOSAPIFactory factory, IUser user, String provider);

    /**
     * Insert a fact for a user.
     *
     * @param factory
     *             idOS API factory
     * @param user
     *            user to store fact for
     * @param fact
     *            the fact with a value that is a numeric double
     * @param factValue
     *            the fact value
     *
     * @return the number of updated rows
     */
    int insertFactForUser(IdOSAPIFactory factory, IUser user, IFact fact, String factValue);

    /**
     * Insert a fact for a user.
     *
     * @param factory
     *             idOS API factory
     * @param user
     *            user to store fact for
     * @param fact
     *            the fact with a value that is a numeric double
     * @param factValue
     *            the fact value
     *
     * @return the number of updated rows
     */
    int insertFactForUser(IdOSAPIFactory factory, IUser user, IFact fact, double factValue);

    /**
     * Insert a fact for a user.
     *
     * @param factory
     *             idOS API factory
     * @param user
     *            user to store fact for
     * @param fact
     *            the fact with a value that is a numeric double
     * @param factValue
     *            the fact value
     *
     * @return the number of updated rows
     */
    int insertFactForUser(IdOSAPIFactory factory, IUser user, IFact fact, boolean factValue);

    /**
     * Obtains all facts for a particular user.
     *
     * @param factory
     *             idOS API factory
     * @param user
     *            desired user to get facts for
     *
     * @return the facts as fact - values.
     */
    HashMap<IFact, String> obtainFactsForUser(IdOSAPIFactory factory, IUser user);

    /**
     * load the profiles for a single user
     *
     * @param factory
     *             idOS API factory
     * @param user the given user
     *
     * @return the list of profiles
     */
    ArrayList<IProfile> obtainSingleUserProfiles(IdOSAPIFactory factory, IUser user);

    /**
     * Obtain binary facts for a given profile and provider combination
     *
     * @param factory idOS API factory
     * @param user user to obtain the facts for
     * @param provider the provider to restrict the search to
     * @return the numeric facts
     */
    HashMap<IFact, Double> obtainBinaryFactsForProfile(IdOSAPIFactory factory, IUser user, String provider);

    /**
     * Obtain a feature value as json
     *
     * @param factory idOS API factory
     * @param user user to obtain a feature value
     * @param provider provider name
     * @param featureName feature name
     * @return the feature value as a json element
     */
    JsonElement obtainFeatureValue(IdOSAPIFactory factory, IUser user, String provider, String featureName);

    /**
     * Obtain a feature value as boolean
     *
     * @param factory idOS API factory
     * @param user user to obtain a feature value
     * @param provider provider name
     * @param featureName feature name
     * @return feature as boolean value
     */
    Boolean obtainBooleanFeatureValue(IdOSAPIFactory factory, IUser user, String provider, String featureName);

    /**
     * Obtain a feature value as a double
     *
     * @param factory idOS API factory
     * @param user user to obtain a feature value
     * @param provider provider name
     * @param featureName feature name
     * @return feature as double value
     */
    double obtainDoubleFeatureValue(IdOSAPIFactory factory, IUser user, String provider, String featureName);

    /**
     * Obtain a feature value as a string
     *
     * @param factory idOS API factory
     * @param user user to obtain a feature value
     * @param provider provider name
     * @param featureName feature name
     * @return feature as a string value
     */
    String obtainStringFeatureValue(IdOSAPIFactory factory, IUser user, String provider, String featureName);

    /**
     * Get facebook email
     *
     * @param factory idOS API factory
     * @param user email for this user
     * @return the email
     */
    String obtainFacebookEmail(IdOSAPIFactory factory, IUser user);

    /**
     * Obtain whether the users paypal account is verified
     *
     * @param factory idOS API factory
     * @param user the given user
     * @return a boolean with the answer
     */
    Boolean obtainFactValueIsPaypalVerified(IdOSAPIFactory factory, IUser user);

    /**
     * Obtain a specific  fact for all providers for a given user
     *
     * @param factory idOS API factory
     * @param user given user
     * @param factName fact name
     * @return a hashmap of fact - value
     */
    HashMap<IFact, String> obtainSpecificFactForUser(IdOSAPIFactory factory, IUser user, String factName);

    /**
     * Obtain numeric facts for a given profile and provider combination
     *
     * @param factory idOS API factory
     * @param user user to obtain the facts for
     * @param provider the provider to restrict the search to
     *
     * @return the numeric facts
     */
    HashMap<IFact, Double> obtainNumericFactsForProfile(IdOSAPIFactory factory, IUser user, String provider);

    /**
     * Upsert score
     *
     * @param factory idOS API factory
     * @param user user to upsert a score
     * @param scoreName name of the score, related to a model
     * @param attribute attribute to which the score is related. null if related to profile
     * @param score double in [0,1] indicating the score
     */
    void upsertScore(IdOSAPIFactory factory, IUser user, String scoreName, String attribute, double score);

    /**
     * Upsert a gate for a user
     * @param factory idOS API factory
     * @param user user to upsert a gate for
     * @param gateName the gate name
     * @param confidenceLevel confidence as a string
     */
    void upsertGate(IdOSAPIFactory factory, IUser user, String gateName, String confidenceLevel);
}
