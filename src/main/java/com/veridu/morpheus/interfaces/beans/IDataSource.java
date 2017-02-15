/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.beans;

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

    void insertFlag(IdOSAPIFactory factory, IUser user, String flagName, String attribute);

    void deleteFlag(IdOSAPIFactory factory, IUser user, String flagName);

    int insertAttributeCandidatesForUser(IdOSAPIFactory factory, IUser user, String attName,
            ArrayList<ICandidate> candidates);

    /**
     * Obtain the facts for a given user and provider. The return is a map of Facts.
     *
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
     * @param user
     *            user to store fact for
     * @param fact
     *            the fact with a value that is a numeric double
     * @param value
     *            the fact value
     *
     * @return the number of updated rows
     */
    int insertFactForUser(IdOSAPIFactory factory, IUser user, IFact fact, String factValue);

    /**
     * Insert a fact for a user.
     *
     * @param user
     *            user to store fact for
     * @param fact
     *            the fact with a value that is a numeric double
     * @param value
     *            the fact value
     *
     * @return the number of updated rows
     */
    int insertFactForUser(IdOSAPIFactory factory, IUser user, IFact fact, double factValue);

    /**
     * Insert a fact for a user.
     *
     * @param user
     *            user to store fact for
     * @param fact
     *            the fact with a value that is a numeric double
     * @param value
     *            the fact value
     *
     * @return the number of updated rows
     */
    int insertFactForUser(IdOSAPIFactory factory, IUser user, IFact fact, boolean factValue);

    /**
     * Obtains all facts for a particular user.
     *
     * @param user
     *            desired user to get facts for
     *
     * @return the facts as fact => values.
     */
    HashMap<IFact, String> obtainFactsForUser(IdOSAPIFactory factory, IUser user);

    /**
     * load the profiles for a single user
     *
     * @param user
     * @return
     */
    ArrayList<IProfile> obtainSingleUserProfiles(IdOSAPIFactory factory, IUser user);

    HashMap<IFact, Double> obtainBinaryFactsForProfile(IdOSAPIFactory factory, IUser user, String provider);

    String obtainFacebookEmail(IdOSAPIFactory factory, IUser user);

    Boolean obtainFactValueIsPaypalVerified(IdOSAPIFactory factory, IUser user);

    HashMap<IFact, String> obtainSpecificFactForUser(IdOSAPIFactory factory, IUser user, String factName);

    HashMap<IFact, Double> obtainNumericFactsForProfile(IdOSAPIFactory factory, IUser user, String provider);

    void upsertScore(IdOSAPIFactory factory, IUser user, String scoreName, String attribute, double score);

    void upsertGate(IdOSAPIFactory factory, IUser user, String gateName, String confidenceLevel);
}
