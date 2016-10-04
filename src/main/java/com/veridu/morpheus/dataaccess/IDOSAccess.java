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
public class IDOSAccess implements IDataSource {

    static Logger logger = Logger.getLogger(IDOSAccess.class.getName());

    private int getLatestSourceIdForProvider(IdOSAPIFactory factory, String userId, String provider) {
        try {
            factory.getSource().setAuthType(IdOSAuthType.HANDLER);
            JsonObject response = factory.getSource().listAll(userId,
                    Filter.createFilter().addSourceNameFilter(provider).addOrderByFilter("created_at")
                            .addSortFilter(SortFilterType.DESC).addLimitFilter(1));
            if (LocalUtils.okResponse(response)) {
                JsonArray data = LocalUtils.getResponseData(response);
                return data.get(0).getAsJsonObject().get("id").getAsInt();
            } else
                logger.error("Could not get latest source id for source " + provider + " for user " + userId);
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public HashMap<IFact, String> obtainProviderFactsForUser(IdOSAPIFactory factory, IUser user, String provider) {
        HashMap<IFact, String> facts = new HashMap<>();

        try {
            factory.getSource().setAuthType(IdOSAuthType.HANDLER);
            JsonObject response = factory.getFeature()
                    .listAll(user.getId(), Filter.createFilter().addFeatureSourceNameFilter(provider));
            if (LocalUtils.okResponse(response)) {
                JsonArray data = LocalUtils.getResponseData(response);
                data.forEach(k -> {
                    JsonObject jobj = k.getAsJsonObject();
                    String sourceName = jobj.get("source").getAsString();
                    facts.put(new Fact(jobj.get("name").getAsString(), sourceName), String.valueOf(jobj.get("value")));
                });
            } else
                logger.error("API error on obtain provider facts for user " + user.getId());
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return facts;
    }

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

        return LocalUtils.okResponse(response) ? 1 : 0;
    }

    @Override
    public int insertAttributeCandidatesForUser(IdOSAPIFactory factory, IUser user, String attName,
            ArrayList<ICandidate> candidates) {
        //JsonObject response = null;

        try {
            factory.getAttribute().setAuthType(IdOSAuthType.HANDLER);
            factory.getAttribute().deleteAll(user.getId(), Filter.createFilter().addNameFilter(attName));
            candidates.parallelStream().forEach(k -> {
                try {
                    factory.getAttribute().create(user.getId(), attName, k.getValue(), k.getSupportScore());
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
                    JsonObject source = jobj.get("source").getAsJsonObject();
                    facts.put(new Fact(jobj.get("name").getAsString(), source.get("source").getAsString()),
                            String.valueOf(jobj.get("value")));
                });
            } else
                logger.error("Could not obtain facts for user " + user.getId());

        } catch (SDKException e) {
            e.printStackTrace();
        }

        return facts;
    }

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
                    IProfile profile = new Profile(obj.get("name").getAsString(), obj.get("id").getAsString());
                    profiles.add(profile);
                });
            } else
                logger.error("Could not obtain single user profiles for user " + user.getId());
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return profiles;
    }

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

    private double parseBoolAsDouble(boolean value) {
        return value ? 1.0 : 0.0;
    }

    private JsonElement obtainFeatureValue(IdOSAPIFactory factory, IUser user, String provider, String featureName) {
        int sourceId = getLatestSourceIdForProvider(factory, user.getId(), provider);
        JsonObject response;

        try {
            response = factory.getFeature().listAll(user.getId(),
                    Filter.createFilter().addSourceIDFilter(sourceId).addNameFilter(featureName));

            if (LocalUtils.okResponse(response)) {
                JsonArray data = LocalUtils.getResponseData(response);
                if (data.size() > 0) {
                    JsonElement elem = data.get(0).getAsJsonObject().get("value");
                    return elem;
                }
            }

        } catch (InvalidToken e) {
            e.printStackTrace();
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Boolean obtainBooleanFeatureValue(IdOSAPIFactory factory, IUser user, String provider, String featureName) {
        JsonElement element = obtainFeatureValue(factory, user, provider, featureName);
        if (element != null)
            return element.getAsBoolean();
        return null;
    }

    private double obtainDoubleFeatureValue(IdOSAPIFactory factory, IUser user, String provider, String featureName) {
        JsonElement element = obtainFeatureValue(factory, user, provider, featureName);
        if (element != null)
            return element.getAsDouble();
        return Double.NaN;
    }

    private String obtainStringFeatureValue(IdOSAPIFactory factory, IUser user, String provider, String featureName) {
        JsonElement element = obtainFeatureValue(factory, user, provider, featureName);
        if (element != null)
            return element.getAsString();
        return "";
    }

    @Override
    public String obtainFacebookEmail(IdOSAPIFactory factory, IUser user) {
        return obtainStringFeatureValue(factory, user, "facebook", "emailAddress");
    }

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

    @Override
    public Boolean obtainFactValueIsPaypalVerified(IdOSAPIFactory factory, IUser user) {
        return obtainBooleanFeatureValue(factory, user, "paypal", "verifiedProfile");
    }

    @Override
    public void deleteWarning(IdOSAPIFactory factory, IUser user, String warningName) {
        try {
            factory.getWarning().setAuthType(IdOSAuthType.HANDLER);
            factory.getWarning().deleteAll(user.getId(), Filter.createFilter().addSlugFilter(warningName));
        } catch (SDKException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertWarning(IdOSAPIFactory factory, IUser user, String warningName, String attribute) {
        try {
            factory.getWarning().setAuthType(IdOSAuthType.HANDLER);
            factory.getWarning().create(user.getId(), warningName, attribute);
        } catch (SDKException e) {
            e.printStackTrace();
        }
    }

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

    @Override
    public void upsertGate(IdOSAPIFactory factory, IUser user, String gateName, boolean pass) {
        try {
            factory.getGate().setAuthType(IdOSAuthType.HANDLER);
            factory.getGate().upsert(user.getId(), gateName, pass);
        } catch (SDKException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
