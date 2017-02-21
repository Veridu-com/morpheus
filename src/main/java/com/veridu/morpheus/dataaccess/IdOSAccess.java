/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.dataaccess;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.idos.exceptions.InvalidToken;
import com.veridu.idos.exceptions.SDKException;
import com.veridu.idos.utils.Filter;
import com.veridu.idos.utils.IdOSAuthType;
import com.veridu.idos.utils.SortFilterType;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.impl.Profile;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.facts.ICandidate;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.users.IProfile;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.LocalUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cassio on 10/2/16.
 */
@Component("idosSQL")
public class IdOSAccess implements IDataSource {

    static Logger logger = Logger.getLogger(IdOSAccess.class.getName());

    /**
     * Gets the most recent source id for a particular provider name
     *
     * @param factory idOS API factory
     * @param userId the user id
     * @param provider provider name
     * @return integer value with id
     */
    private int getLatestSourceIdForProvider(IdOSAPIFactory factory, String userId, String provider) {
        try {
            factory.getSource().setAuthType(IdOSAuthType.HANDLER);
            JsonObject response = factory.getSource().listAll(userId,
                    Filter.createFilter().addSourceNameFilter(provider).addOrderByFilter("created_at")
                            .addSortFilter(SortFilterType.DESC).addLimitFilter(1));
            if (LocalUtils.okResponse(response)) {
                JsonArray data = LocalUtils.getResponseData(response);
                if (data.size() > 0 && !data.get(0).getAsJsonObject().get("id").isJsonNull())
                    return data.get(0).getAsJsonObject().get("id").getAsInt();
            } else
                logger.error("Could not get latest source id for source " + provider + " for user " + userId);
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Obtain provider facts for a user
     *
     * @param factory  idOS API factory
     * @param user
     *            will use user_id as profile_id for query
     * @param provider
     *            desired provider of facts
     * @return the facts as a hashmap of IFact - String
     */
    @Override
    public HashMap<IFact, String> obtainProviderFactsForUser(IdOSAPIFactory factory, IUser user, String provider) {
        HashMap<IFact, String> facts = new HashMap<>();

        try {
            factory.getFeature().setAuthType(IdOSAuthType.HANDLER);
            JsonObject response = factory.getFeature()
                    .listAll(user.getId(), Filter.createFilter().addFeatureSourceNameFilter(provider));

            if (LocalUtils.okResponse(response)) {
                JsonArray data = LocalUtils.getResponseData(response);
                data.forEach(k -> {
                    JsonObject jobj = k.getAsJsonObject();
                    if (LocalUtils.validateJsonField(jobj, "source")) {
                        String sourceName = jobj.get("source").getAsString();
                        if (LocalUtils.validateJsonField(jobj, "name") && LocalUtils.validateJsonField(jobj, "value"))
                            facts.put(new Fact(jobj.get("name").getAsString(), sourceName),
                                    String.valueOf(jobj.get("value")));
                    }
                });
            } else
                logger.error("API error on obtain provider facts for user " + user.getId());
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return facts;
    }

    /**
     * Delete facts for a given user related to a specific provider
     *
     * @param factory idOS API factory
     * @param user
     *            will use the userId as profileId in the facts table
     * @param provider
     *            the provider for whom the facts should be wiped
     * @return the number of deleted facts
     */
    @Override
    public int deleteProviderFactsForUser(IdOSAPIFactory factory, IUser user, String provider) {
        JsonObject response = null;

        try {
            factory.getFeature().setAuthType(IdOSAuthType.HANDLER);
            response = factory.getFeature()
                    .deleteAll(user.getId(), Filter.createFilter().addFeatureSourceNameFilter(provider));
        } catch (SDKException e) {
            e.printStackTrace();
        }

        if (LocalUtils.okResponse(response))
            return response.get("deleted").getAsInt();

        return 0;
    }

    /**
     * Insert attribute candidates for a user and a given attribute name
     *
     * @param factory idOS API factory
     * @param user the given user
     * @param attName attribute name
     * @param candidates the candidate values to insert in a list
     * @return 1 if the operation succeeded
     */
    @Override
    public int insertAttributeCandidatesForUser(IdOSAPIFactory factory, IUser user, String attName,
            ArrayList<ICandidate> candidates) {

        try {
            factory.getCandidate().setAuthType(IdOSAuthType.HANDLER);
            factory.getCandidate()
                    .deleteAll(user.getId(), Filter.createFilter().addCandidateAttributeNameFilter(attName));
            candidates.parallelStream().forEach(k -> {
                try {
                    factory.getCandidate().create(user.getId(), attName, k.getValue(), k.getSupportScore());
                } catch (SDKException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });

        } catch (SDKException e) {
            e.printStackTrace();
        }

        return 1;
    }

    /**
     *
     * Insert fact for user
     *
     * @param factory idOS API factory
     * @param user
     *            user to store fact for
     * @param fact
     *            the fact containing name and provider
     * @param factValue the fact value
     * @return 1 if the operation succeeded, 0 otherwise
     *
     */
    @Override
    public int insertFactForUser(IdOSAPIFactory factory, IUser user, IFact fact, String factValue) {
        JsonObject response = null;

        try {
            factory.getFeature().setAuthType(IdOSAuthType.HANDLER);
            response = factory.getFeature().upsert(user.getId(), fact.getName(), factValue);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidToken e) {
            e.printStackTrace();
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return LocalUtils.okResponse(response) ? 1 : 0;
    }

    /**
     *
     * @param factory idOS API factory
     * @param user
     *            user to store fact for
     * @param fact
     *            the fact containing name and provider
     * @param factValue the fact value
     * @return 1 if the operation succeeded, 0 otherwise
     */
    @Override
    public int insertFactForUser(IdOSAPIFactory factory, IUser user, IFact fact, double factValue) {
        JsonObject response = null;

        try {
            factory.getFeature().setAuthType(IdOSAuthType.HANDLER);
            response = factory.getFeature().upsert(user.getId(), fact.getName(), factValue);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidToken e) {
            e.printStackTrace();
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return LocalUtils.okResponse(response) ? 1 : 0;
    }

    /**
     *
     * @param factory idOS API factory
     * @param user
     *            user to store fact for
     * @param fact
     *            the fact containing name and provider
     * @param factValue the fact value
     * @return 1 if the operation succeeded, 0 otherwise
     */
    @Override
    public int insertFactForUser(IdOSAPIFactory factory, IUser user, IFact fact, boolean factValue) {
        JsonObject response = null;

        try {
            factory.getFeature().setAuthType(IdOSAuthType.HANDLER);
            response = factory.getFeature().upsert(user.getId(), fact.getName(), factValue);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidToken e) {
            e.printStackTrace();
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return LocalUtils.okResponse(response) ? 1 : 0;
    }

    /**
     * Obtain all facts related to a user
     *
     * @param factory idOS API factory
     * @param user
     *            desired user to get facts for
     *
     * @return facts as a hashmap: IFact - String
     */
    @Override
    public HashMap<IFact, String> obtainFactsForUser(IdOSAPIFactory factory, IUser user) {
        HashMap<IFact, String> facts = new HashMap<>();

        try {
            factory.getFeature().setAuthType(IdOSAuthType.HANDLER);
            JsonObject response = factory.getFeature().listAll(user.getId());

            if (LocalUtils.okResponse(response)) {
                JsonArray data = LocalUtils.getResponseData(response);
                data.forEach(k -> {
                    JsonObject jobj = k.getAsJsonObject();
                    if (LocalUtils.validateJsonField(jobj, "source")) {
                        JsonObject source = jobj.get("source").getAsJsonObject();
                        if (LocalUtils.validateJsonField(jobj, "name") && LocalUtils.validateJsonField(source, "source")
                                && LocalUtils.validateJsonField(jobj, "value"))
                            facts.put(new Fact(jobj.get("name").getAsString(), source.get("source").getAsString()),
                                    String.valueOf(jobj.get("value")));
                    }
                });
            } else
                logger.error("Could not obtain facts for user " + user.getId());

        } catch (SDKException e) {
            e.printStackTrace();
        }

        return facts;
    }

    /**
     * Obtain profiles for a specific user
     *
     * @param factory idOS API factory
     * @param user the user to find profiles
     *
     * @return a list with all profiles
     */
    @Override
    public ArrayList<IProfile> obtainSingleUserProfiles(IdOSAPIFactory factory, IUser user) {
        ArrayList<IProfile> profiles = new ArrayList<>();
        try {
            factory.getSource().setAuthType(IdOSAuthType.HANDLER);
            JsonObject response = factory.getSource().listAll(user.getId());
            if (LocalUtils.okResponse(response)) {
                JsonArray data = LocalUtils.getResponseData(response);
                data.forEach(k -> {
                    JsonObject obj = k.getAsJsonObject();
                    if (LocalUtils.validateJsonField(obj, "name") && LocalUtils.validateJsonField(obj, "id")) {
                        IProfile profile = new Profile(obj.get("name").getAsString(), obj.get("id").getAsString());
                        profiles.add(profile);
                    }
                });
            } else
                logger.error("Could not obtain single user profiles for user " + user.getId());
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return profiles;
    }

    /**
     * Obtain numeric facts for a given profile and provider combination
     *
     * @param factory idOS API factory
     * @param user user to obtain the facts for
     * @param provider the provider to restrict the search to
     *
     * @return the numeric facts
     */
    @Override
    public HashMap<IFact, Double> obtainNumericFactsForProfile(IdOSAPIFactory factory, IUser user, String provider) {
        int sourceId = getLatestSourceIdForProvider(factory, user.getId(), provider);
        HashMap<IFact, Double> facts = new HashMap<>();

        try {
            JsonObject response = factory.getFeature().listAll(user.getId(),
                    Filter.createFilter().addFeatureSourceNameFilter(provider).addNameFilter("num*"));

            if (LocalUtils.okResponse(response)) {
                JsonArray data = LocalUtils.getResponseData(response);
                data.forEach(k -> {
                    JsonObject jobj = k.getAsJsonObject();
                    if (LocalUtils.validateJsonField(jobj, "name") && LocalUtils.validateJsonField(jobj, "value"))
                        facts.put(new Fact(jobj.get("name").getAsString(), provider), jobj.get("value").getAsDouble());
                });
            }

        } catch (InvalidToken e) {
            e.printStackTrace();
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return facts;
    }

    /**
     * Obtain binary facts for a given profile and provider combination
     *
     * @param factory idOS API factory
     * @param user user to obtain the facts for
     * @param provider the provider to restrict the search to
     * @return the numeric facts
     */
    @Override
    public HashMap<IFact, Double> obtainBinaryFactsForProfile(IdOSAPIFactory factory, IUser user, String provider) {
        int sourceId = getLatestSourceIdForProvider(factory, user.getId(), provider);
        HashMap<IFact, Double> facts = new HashMap<>();

        try {
            JsonObject response = factory.getFeature().listAll(user.getId(),
                    Filter.createFilter().addFeatureSourceNameFilter(provider).addNameFilter("is*"));

            if (LocalUtils.okResponse(response)) {
                JsonArray data = LocalUtils.getResponseData(response);
                data.forEach(k -> {
                    JsonObject jobj = k.getAsJsonObject();
                    if (LocalUtils.validateJsonField(jobj, "name") && LocalUtils.validateJsonField(jobj, "value"))
                        facts.put(new Fact(jobj.get("name").getAsString(), provider),
                                parseBoolAsDouble(jobj.get("value").getAsBoolean()));
                });
            }

        } catch (InvalidToken e) {
            e.printStackTrace();
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return facts;
    }

    /**
     * Convert a boolean value to a double
     *
     * @param value boolean true/false
     *
     * @return Return 1 or 0  based on the boolean value
     */
    private double parseBoolAsDouble(boolean value) {
        return value ? 1.0 : 0.0;
    }

    /**
     * Obtain a specific feature given its name for a specific user and provider
     *
     * @param factory idOS API factory
     * @param user the given user
     * @param provider a provider name
     * @param featureName the feature name
     *
     * @return a json with the feature
     */
    private JsonElement obtainFeatureValue(IdOSAPIFactory factory, IUser user, String provider, String featureName) {
        int sourceId = getLatestSourceIdForProvider(factory, user.getId(), provider);
        JsonObject response;

        try {
            response = factory.getFeature().listAll(user.getId(),
                    Filter.createFilter().addSourceIDFilter(sourceId).addNameFilter(featureName));

            if (LocalUtils.okResponse(response)) {
                JsonArray data = LocalUtils.getResponseData(response);
                if (data != null && data.size() > 0) {
                    JsonObject firstElement = data.get(0).getAsJsonObject();
                    if (LocalUtils.validateJsonField(firstElement, "value")) {
                        JsonElement elem = firstElement.get("value");
                        return elem;
                    }
                }
            }

        } catch (InvalidToken e) {
            e.printStackTrace();
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Obtain a boolean feature value
     *
     * @param factory idOS API factory
     * @param user user for the feature
     * @param provider provider name
     * @param featureName feature name
     * @return boolean json value
     */
    private Boolean obtainBooleanFeatureValue(IdOSAPIFactory factory, IUser user, String provider, String featureName) {
        JsonElement element = obtainFeatureValue(factory, user, provider, featureName);
        if (LocalUtils.validateJsonElement(element))
            return element.getAsBoolean();
        return null;
    }

    /**
     * Obtain a double feature value
     *
     * @param factory idOS API factory
     * @param user user for the feature
     * @param provider provider name
     * @param featureName feature name
     *
     * @return double value
     */
    private double obtainDoubleFeatureValue(IdOSAPIFactory factory, IUser user, String provider, String featureName) {
        JsonElement element = obtainFeatureValue(factory, user, provider, featureName);
        if (LocalUtils.validateJsonElement(element))
            return element.getAsDouble();
        return Double.NaN;
    }

    /**
     * Obtain a string feature value
     *
     * @param factory idOS API factory
     * @param user user for the feature
     * @param provider provider name
     * @param featureName feature name
     *
     * @return String feature value
     */
    private String obtainStringFeatureValue(IdOSAPIFactory factory, IUser user, String provider, String featureName) {
        JsonElement element = obtainFeatureValue(factory, user, provider, featureName);
        if (LocalUtils.validateJsonElement(element))
            return element.getAsString();
        return "";
    }

    /**
     * Get facebook email
     *
     * @param factory idOS API factory
     * @param user email for this user
     * @return the email
     */
    @Override
    public String obtainFacebookEmail(IdOSAPIFactory factory, IUser user) {
        return obtainStringFeatureValue(factory, user, "facebook", "emailAddress");
    }

    /**
     * Obtain a specific  fact for all providers for a given user
     *
     * @param factory idOS API factory
     * @param user given user
     * @param factName fact name
     * @return a hashmap of fact - value
     */
    @Override
    public HashMap<IFact, String> obtainSpecificFactForUser(IdOSAPIFactory factory, IUser user, String factName) {
        HashMap<IFact, String> facts = new HashMap<>();

        try {
            factory.getFeature().setAuthType(IdOSAuthType.HANDLER);
            JsonObject response = factory.getFeature()
                    .listAll(user.getId(), Filter.createFilter().addNameFilter(factName));
            if (LocalUtils.okResponse(response)) {
                JsonArray data = LocalUtils.getResponseData(response);
                data.forEach(k -> {
                    JsonObject jobj = k.getAsJsonObject();
                    if (LocalUtils.validateJsonField(jobj, "name") && LocalUtils.validateJsonField(jobj, "source")
                            && LocalUtils.validateJsonField(jobj, "value"))
                        facts.put(new Fact(jobj.get("name").getAsString(), jobj.get("source").getAsString()),
                                jobj.get("value").getAsString());
                });
            } else
                logger.error("Could not get specific fact " + factName + " for user " + user.getId());
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return facts;
    }

    /**
     * Obtain whether the users paypal account is verified
     *
     * @param factory idOS API factory
     * @param user the given user
     * @return a boolean with the answer
     */
    @Override
    public Boolean obtainFactValueIsPaypalVerified(IdOSAPIFactory factory, IUser user) {
        return obtainBooleanFeatureValue(factory, user, "paypal", "verifiedProfile");
    }

    /**
     * Delete a flag for a given user
     *
     * @param factory idOS API factory
     * @param user given user
     * @param flagName flag name to delete
     *
     */
    @Override
    public void deleteFlag(IdOSAPIFactory factory, IUser user, String flagName) {
        try {
            factory.getFlag().setAuthType(IdOSAuthType.HANDLER);
            factory.getFlag().deleteAll(user.getId(), Filter.createFilter().addSlugFilter(flagName));
        } catch (SDKException e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert a flag for a given user
     *
     * @param factory idOS API factory
     * @param user given user
     * @param flagName the flag name
     * @param attribute the attribute name
     */
    @Override
    public void insertFlag(IdOSAPIFactory factory, IUser user, String flagName, String attribute) {
        try {
            factory.getFlag().setAuthType(IdOSAuthType.HANDLER);
            factory.getFlag().create(user.getId(), flagName, attribute);
        } catch (SDKException e) {
            e.printStackTrace();
        }
    }

    /**
     * Upsert score
     *
     * @param factory idOS API factory
     * @param user user to upsert a score
     * @param scoreName name of the score, related to a model
     * @param attribute attribute to which the score is related. null if related to profile
     * @param score double in [0,1] indicating the score
     */
    @Override
    public void upsertScore(IdOSAPIFactory factory, IUser user, String scoreName, String attribute, double score) {
        try {
            factory.getScore().setAuthType(IdOSAuthType.HANDLER);
            factory.getScore().upsert(user.getId(), scoreName, attribute, score);
        } catch (SDKException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Upsert a gate for a user
     *
     * @param factory idOS API factory
     * @param user user to upsert a gate for
     * @param gateName the gate name
     * @param confidenceLevel confidence as a string
     */
    @Override
    public void upsertGate(IdOSAPIFactory factory, IUser user, String gateName, String confidenceLevel) {
        try {
            factory.getGate().setAuthType(IdOSAuthType.HANDLER);
            factory.getGate().upsert(user.getId(), gateName, confidenceLevel);
        } catch (SDKException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
