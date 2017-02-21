/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.veridu.morpheus.impl.Candidate;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.interfaces.facts.ICandidate;
import com.veridu.morpheus.interfaces.facts.IFact;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import weka.core.Instance;
import weka.core.Utils;

import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by cassio on 10/2/16.
 */
public class LocalUtils {

    /**
     * Validate a json element is not null
     * @param elem the json element
     * @return true if not null
     */
    public static boolean validateJsonElement(JsonElement elem) {
        return elem != null && !elem.isJsonNull();
    }

    /**
     * Validate a json object is not null
     * @param jobj the json object
     * @return true if not null
     */
    public static boolean validateJsonObject(JsonObject jobj) {
        return jobj != null && !jobj.isJsonNull();
    }

    /**
     * Validate a json field
     * @param jobj the json object
     * @param fieldName name of the field to validate
     * @return true if not null
     */
    public static boolean validateJsonField(JsonObject jobj, String fieldName) {
        return validateJsonObject(jobj) && jobj.has(fieldName) && !jobj.get(fieldName).isJsonNull();
    }

    /**
     * Validate request parameters are not null or empty
     * @param params request parameters
     * @return true if ok
     */
    public static boolean validateRequestParams(Parameters params) {
        return params.userName != null && params.userName.length() > 0 && params.publicKey != null
                && params.publicKey.length() > 0;
    }

    /**
     * Check whether an idOS API response status is ok
     * @param response idOS API response
     * @return true if ok
     */
    public static boolean okResponse(JsonObject response) {
        if (response == null)
            return false;
        return response.get("status").getAsBoolean();
    }

    //    public static boolean authenticate(HttpHeaders headers) {
    //        String authKey = "Authorization";
    //        if (headers.getRequestHeaders().containsKey(authKey))
    //            return headers.getRequestHeaders().getFirst(authKey).equals(Constants.HTTP_BASIC_AUTH_TOKEN);
    //        return false;
    //    }

    /**
     * Get data field from response object as an array
     * @param response idOS API response
     * @return data field as array
     */
    public static com.google.gson.JsonArray getResponseData(JsonObject response) {
        return LocalUtils.validateJsonField(response, "data") ? response.get("data").getAsJsonArray() : null;
    }

    /**
     * Check day and month of two date objects match
     *
     * @param date1 first date
     * @param date2 second date
     * @return true if the day and month of both dates match
     */
    public static boolean checkDayMonthMatch(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.setTime(date1);
        cal2.setTime(date2);

        return (cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) && (cal1.get(Calendar.MONTH) == cal2
                .get(Calendar.MONTH));

    }

    /**
     * Check if an instance has all attributes missing
     *
     * @param inst an instance object
     * @return true if everything is missing
     */
    public static boolean allAttributesMissing(Instance inst) {
        for (int i = 0; i < inst.numAttributes(); i++)
            if (!inst.isMissing(i))
                return false;
        return true;
    }

    /**
     * Parse an ISO date to a java Date object
     * @param isoDate a facebook ISO date string
     * @return a Date object
     */
    public static Date parseFacebookRFC822Date(String isoDate) {
        // see http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
        // and
        // http://stackoverflow.com/questions/3914404/how-to-get-current-moment-in-iso-8601-format
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try {
            return df.parse(isoDate);
        } catch (ParseException e) {
            // e.printStackTrace();
        }
        return null;
    }

    /**
     * This method may actually return null if there is a problem parsing the date.
     *
     * @param fbkDate a date
     * @return date parse into an object
     */
    public static Date parseFacebookDate(String fbkDate) {
        if (fbkDate == null)
            return null;
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        try {
            return df.parse(fbkDate);
        } catch (ParseException e) {
            // e.printStackTrace();
        }
        return null;
    }

    /**
     * Unserialize a PHP string
     *
     * @param phpSerialized the serialized string
     * @return the unserialized string
     */
    public static String phpStringUnserialize(String phpSerialized) {
        int indexOfFirstColon = phpSerialized.indexOf(":\"");
        int indexOfSecondColon = phpSerialized.indexOf("\";", indexOfFirstColon + 1);
        return phpSerialized.substring(indexOfFirstColon + 2, indexOfSecondColon);
    }

    /**
     * Extract the domain from an e-mail address
     *
     * @param email the email address
     * @return the domain
     */
    public static String extractDomain(String email) {
        int indexOfAt = email.indexOf("@");
        return email.substring(indexOfAt + 1, email.length());
    }

    /**
     * Removes all accents from a name and returns it all in lower case.
     *
     * @param name given name
     * @return name with all accents removed and in lower case
     */
    public static String normalizeName(String name) {
        if (name != null) {
            String s = Normalizer.normalize(name, Normalizer.Form.NFD);
            s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
            return s.toLowerCase();
        }
        return null;
    }

    /**
     * Set an integer value at a position in an instance
     *
     * @param inst an instance object
     * @param pos the position on which to insert the value
     * @param value value to insert
     */
    public static void setIntegerValueAtPos(Instance inst, final int pos, Integer value) {
        if (value != null)
            inst.setValue(pos, new Double(value));
        else
            inst.setValue(pos, Utils.missingValue());
    }

    /**
     * Set a binary value at a position in an instance
     *
     * @param inst an instance object
     * @param pos the position on which to insert the value
     * @param comparison true or false
     */
    public static void setBinaryValueAtPos(Instance inst, final int pos, Boolean comparison) {
        if (comparison != null) {
            if (comparison)
                inst.setValue(pos, "1");
            else
                inst.setValue(pos, "0");
        } else
            inst.setValue(pos, Utils.missingValue());
    }

    /**
     * Compute the normalized Levenshtein similarity between two strings
     *
     * @param s1 first string
     * @param s2 second string
     * @return the normalized similarity in [0,1] - 0 means completely different strings
     */
    public static double normalizedLevenshteinSimilarity(String s1, String s2) {
        if ((s1 != null) && (s2 != null))
            return new NormalizedLevenshtein().similarity(s1, s2);
        return 0;
    }

    /**
     * Apply a name similarity rule
     *
     * @param inst input instance
     * @param pos position of feature
     * @param opCode operation code
     * @param s1 first string
     * @param s2 second string
     * @param threshold threshold to consider both strings similar
     */
    public static void applyNameSimRule(Instance inst, final int pos, final String opCode, final String s1,
            final String s2, double threshold) {
        switch (opCode) {
        case "=":
            setBinaryValueAtPos(inst, pos, normalizedLevenshteinSimilarity(s1, s2) >= threshold);
            break;
        }
    }

    //    public static JsonArray createCandidatesJSON(ArrayList<ICandidate> candidates) {
    //        JsonArrayBuilder arBuilder = Json.createArrayBuilder();
    //
    //        for (ICandidate candidate : candidates) {
    //            JsonObjectBuilder candBuilder = Json.createObjectBuilder();
    //            candBuilder.add("value", candidate.getValue());
    //            candBuilder.add("support", candidate.getSupportScore());
    //            arBuilder.add(candBuilder.build());
    //        }
    //
    //        return arBuilder.build();
    //    }

    /**
     * Normalize candidates scores so they form a probability distribution
     *
     * @param cands candidates hashmap containing value and support
     * @return a list of candidates
     */
    public static ArrayList<ICandidate> normalizeCandidatesScores(HashMap<String, Double> cands) {
        ArrayList<ICandidate> candidates = new ArrayList<>();

        double totalSum = 0;

        for (String cand : cands.keySet())
            totalSum += cands.get(cand);

        for (String cand : cands.keySet())
            candidates.add(new Candidate(cand, cands.get(cand) / totalSum));

        candidates.sort(Comparator.comparing(ICandidate::getSupportScore));

        Collections.reverse(candidates);

        return candidates;
    }

    /**
     * Generate provider facts list
     *
     * @param providers list of provider names
     * @param factName a fact name
     *
     * @return the list of facts given a factname for all providers
     */
    public static ArrayList<IFact> generateProvidersFact(final String[] providers, final String factName) {
        ArrayList<IFact> providerFacts = new ArrayList<>();
        for (String provider : providers)
            providerFacts.add(new Fact(factName, provider));
        return providerFacts;
    }

}
