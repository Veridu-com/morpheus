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

public interface IUtils {

    public HashMap<String, String> generateCredentials(String credentialPubKey, String username);

    /**
     * Finds the index of the minority class for a dataset.
     *
     * @param dataset
     *            instances
     * @return index
     */
    public int obtainMinorityClassIndex(Instances dataset);

    /**
     * Reads the provider facts CSV file. Right now it contains a list of type <provider,factName>, in which only binary
     * or numeric facts are being used.
     *
     * @return a list with facts
     */
    public ArrayList<IFact> readUniqueFactsNumericBinary();

    /**
     * Generates a dataset with 0 instances based on a list of facts
     *
     * @param facts
     *            the facts list
     * @return dataset
     */
    public Instances generateDatasetHeader(ArrayList<IFact> facts);

    /**
     * Create an instance for a dataset (does NOT add it to the dataset though).
     *
     * @param dataset
     *            the dataset header
     * @param facts
     *            the facts list
     * @param user
     *            a user to be added
     *
     * @return created instance
     */
    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, ArrayList<IFact> facts, IUser user);

    /**
     * Returns 0 if p0 is greater than p1
     *
     * @param p0
     * @param p1
     * @return
     */
    public int binaryArgMax(double p0, double p1);

    /**
     * The same as the other binaryArgMax, but using an array with ONLY 2 elements.
     *
     * @param probabilities
     * @return
     */
    public int binaryArgMax(double[] probabilities);

    /**
     * Generic version of read unique facts
     *
     * @param streamPath
     * @return
     */
    public ArrayList<IFact> readFacts(String streamPath);

    /**
     * Read an ARFF file from the filesystem. WARNING: This function will automatically set the class index to be the
     * last attribute.
     *
     * @param filePath
     * @return
     */
    public Instances readARFF(String filePath);

    public IModel readModel(String resourcePath);

    public String getProfileId(ArrayList<IProfile> profiles, String provider);

    public ArrayList<Attribute> createEmptyAndClassAttribute();

    public Instances mergeInstancesHeaders(Instances... headers);

    public Instance mergeInstances(Instances masterHeader, Instance... instances);

    public boolean isDomainInTemporaryDomainsList(String domain);

    public ArrayList<IFact> getChecksBinaryFacts();

    public ArrayList<IFact> getFacebookNumericFacts();

    public ArrayList<IFact> getGoogleBinaryFacts();

    public ArrayList<IFact> getGoogleNumericFacts();

    public ArrayList<IFact> getLinkedinNumericFacts();

    public ArrayList<IFact> getTwitterNumericFacts();

    public ArrayList<IFact> getPaypalBinaryFacts();

    public IdOSAPIFactory getIdOSAPIFactory(HashMap<String, String> credentials);

}
