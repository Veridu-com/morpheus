/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.beans;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.models.IModel;
import com.veridu.morpheus.interfaces.users.IProfile;
import com.veridu.morpheus.interfaces.users.IUser;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Defines generic utilities methods the application may use
 */
public interface IUtils {

    /**
     * Check if candidates exist for a given attribute
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param attributeName attribute name
     * @return true if there are any candidates for the attribute
     */
    boolean checkIfCandidatesExist(IdOSAPIFactory factory, IUser user, String attributeName);

    /**
     * Generate credentials for using the idOS API, such as querying information
     * of a user from a company
     *
     * @param credentialPubKey a credential public key
     * @param username user name
     * @return a hashmap containing the credentials, specifically credentialPublicKey, servicePrivateKey, servicePublicKey and username.
     */
    HashMap<String, String> generateCredentials(String credentialPubKey, String username);

    /**
     * Finds the index of the minority class for a dataset.
     *
     * @param dataset
     *            instances
     * @return index
     */
    int obtainMinorityClassIndex(Instances dataset);

    /**
     * Reads the provider facts CSV file. Right now it contains a list of type provider,factName, in which only binary
     * or numeric facts are being used.
     *
     * @return a list with facts
     */
    ArrayList<IFact> readUniqueFactsNumericBinary();

    /**
     * Generates a dataset with 0 instances based on a list of facts
     *
     * @param facts
     *            the facts list
     * @return dataset
     */
    Instances generateDatasetHeader(ArrayList<IFact> facts);

    /**
     * Returns 0 if p0 is greater than p1
     *
     * @param p0 first probability
     * @param p1 second probability
     *
     * @return 0 if p0 is greater than p1
     */
    int binaryArgMax(double p0, double p1);

    /**
     * The same as the other binaryArgMax, but using an array with ONLY 2 elements.
     *
     * @param probabilities two element probability array
     * @return the position of the greater of the two
     */
    int binaryArgMax(double[] probabilities);

    /**
     * Generic version of read unique facts
     *
     * @param streamPath path of the stream
     *
     * @return a list of facts
     */
    ArrayList<IFact> readFacts(String streamPath);

    /**
     * Read an ARFF file from the filesystem. WARNING: This function will automatically set the class index to be the
     * last attribute.
     *
     * @param filePath path to the file
     * @return all instances in the file
     */
    Instances readARFF(String filePath);

    /**
     * Read a model from the filesystem.
     *
     * @param resourcePath path to the model file
     * @return the loaded model
     */
    IModel readModel(String resourcePath);

    /**
     * Get the profile id of a selected provider given the list of profiles
     *
     * @param profiles the list of profiles
     * @param provider a provider name
     * @return the profile id for that provider
     */
    String getProfileId(ArrayList<IProfile> profiles, String provider);

    /**
     * Creates the isEverythingEmpty attribute, which identifies an instance with all attributes missing.
     * Also creates the class attribute for an instance.
     *
     * @return an attribute list with the isEverythingEmpty and class attributes
     */
    ArrayList<Attribute> createEmptyAndClassAttribute();

    /**
     * Merge attribute information from several dataset instances
     *
     * @param headers a sequence of all dataset headers
     *
     * @return the merged header
     */
    Instances mergeInstancesHeaders(Instances... headers);

    /**
     * Merge a sequence of instances using a master header
     *
     * @param masterHeader common dataset header for all instances
     * @param instances sequence of instances
     * @return the merged data set
     */
    Instance mergeInstances(Instances masterHeader, Instance... instances);

    /**
     * Is the domain in a temporary list
     *
     * @param domain domain name for the email address
     * @return true if the domain of the email is from a temporary mail address
     */
    boolean isDomainInTemporaryDomainsList(String domain);

    /**
     * Get the list of binary facts
     * @return the list of binary facts
     */
    ArrayList<IFact> getChecksBinaryFacts();

    /**
     * Get the list of facebook numeric facts
     * @return the list of facebook numeric facts
     */
    ArrayList<IFact> getFacebookNumericFacts();

    /**
     * Get the list of google binary facts
     * @return the list of google binary facts
     */
    ArrayList<IFact> getGoogleBinaryFacts();

    /**
     * Get the list of google numeric facts
     * @return the list of google numeric facts
     */
    ArrayList<IFact> getGoogleNumericFacts();

    /**
     * Get the list of linkedin numeric facts
     * @return the list of linkedin numeric facts
     */
    ArrayList<IFact> getLinkedinNumericFacts();

    /**
     * Get the list of twitter numeric facts
     * @return the list of twitter numeric facts
     */
    ArrayList<IFact> getTwitterNumericFacts();

    /**
     * Get the list of paypal binary facts
     * @return the list of paypal binary facts
     */
    ArrayList<IFact> getPaypalBinaryFacts();

    /**
     * Get the idOS API factory already configured with the set of credentials
     * @see #generateCredentials(String, String)
     * @param credentials a hashmap of credentials
     * @return the configured idOS API factory
     *
     */
    IdOSAPIFactory getIdOSAPIFactory(HashMap<String, String> credentials);

}
