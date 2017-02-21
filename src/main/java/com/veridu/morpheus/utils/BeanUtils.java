/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.idos.exceptions.InvalidToken;
import com.veridu.idos.exceptions.SDKException;
import com.veridu.idos.utils.Filter;
import com.veridu.idos.utils.IdOSAuthType;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.models.IModel;
import com.veridu.morpheus.interfaces.users.IProfile;
import com.veridu.morpheus.interfaces.users.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import weka.core.*;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Service
@Scope("singleton")
public class BeanUtils implements IUtils {

    private BeanConfigurationManager configBean;

    //private IDataSource dao;

    private ArrayList<IFact> uniqueNumericBinaryFacts;

    private ArrayList<IFact> checksBinaryFacts;

    private ArrayList<IFact> googleBinaryFacts;

    private ArrayList<IFact> googleNumericFacts;

    private ArrayList<IFact> facebookNumericFacts;

    private ArrayList<IFact> linkedinNumericFacts;

    private ArrayList<IFact> twitterNumericFacts;

    private ArrayList<IFact> paypalBinaryFacts;

    private HashSet<String> temporaryEmailsList;

    private HashMap<String, IModel> serializedModels;

    /**
     * Constructor
     * @param configBean injected configuration bean
     */
    @Autowired
    public BeanUtils(BeanConfigurationManager configBean) {
        this.configBean = configBean;
    }

    /**
     * called after bean is constructed
     */
    @PostConstruct
    private void init() {
        // read unique and binary facts
        this.uniqueNumericBinaryFacts = readFacts("/csvs/unique-provider-facts-numeric-boolean.csv");
        this.googleBinaryFacts = readFacts("/csvs/google-binary.csv");
        this.googleNumericFacts = readFacts("/csvs/google-numeric.csv");
        this.facebookNumericFacts = readFacts("/csvs/facebook-numeric.csv");
        this.checksBinaryFacts = readFacts("/csvs/checks-binary.csv");
        this.linkedinNumericFacts = readFacts("/csvs/linkedin-numeric.csv");
        this.twitterNumericFacts = readFacts("/csvs/twitter-numeric.csv");
        this.temporaryEmailsList = readTemporaryEmailDomains("/csvs/temporary-emails.csv");
        this.paypalBinaryFacts = readFacts("/csvs/paypal-binary.csv");

        // FIXME speed improvements should come from this
        //    preloadSerializedModels();
    }

    //    public DriverManagerDataSource getFakeUsDataSource() {
    //        DriverManagerDataSource dataSource = new DriverManagerDataSource();
    //        dataSource.setDriverClassName(configBean.getObjectStringProperty("db", "classname"));
    //        dataSource.setUrl(configBean.getObjectStringProperty("db", "url"));
    //        dataSource.setUsername(configBean.getObjectStringProperty("db", "username"));
    //        dataSource.setPassword(configBean.getObjectStringProperty("db", "password"));
    //        return dataSource;
    //    }

    /**
     * Check if candidates exist for a given attribute
     *
     * @param factory idOS API factory
     * @param user selected user
     * @param attributeName attribute name
     * @return true if there are any candidates for the attribute
     */
    @Override
    public boolean checkIfCandidatesExist(IdOSAPIFactory factory, IUser user, String attributeName) {
        try {

            factory.getCandidate().setAuthType(IdOSAuthType.HANDLER);
            JsonObject response = factory.getCandidate()
                    .listAll(user.getId(), Filter.createFilter().addCandidateAttributeNameFilter(attributeName));

            if (response != null && LocalUtils.okResponse(response)) {
                JsonArray data = LocalUtils.getResponseData(response);
                if (data != null && data.size() > 0)
                    return true;
            }

        } catch (InvalidToken invalidToken) {
            invalidToken.printStackTrace();
        } catch (SDKException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Generate credentials for using the idOS API, such as querying information
     * of a user from a company
     *
     * @param credentialPubKey a credential public key
     * @param username user name
     * @return a hashmap containing the credentials, specifically credentialPublicKey, servicePrivateKey, servicePublicKey and username.
     */
    @Override
    public HashMap<String, String> generateCredentials(String credentialPubKey, String username) {
        HashMap<String, String> credentials = new HashMap<>();
        credentials.put("credentialPublicKey", credentialPubKey);
        credentials.put("servicePrivateKey", configBean.getHandlerPrivateKey());
        credentials.put("servicePublicKey", configBean.getHandlerPublicKey());
        credentials.put("username", username);
        return credentials;
    }

    /**
     * Is the domain in a temporary list
     *
     * @param domain domain name for the email address
     * @return true if the domain of the email is from a temporary mail address
     */
    @Override
    public boolean isDomainInTemporaryDomainsList(String domain) {
        return this.temporaryEmailsList.contains(domain);
    }

    private HashSet<String> readTemporaryEmailDomains(String streamPath) {
        InputStream fileStream = this.getClass().getResourceAsStream(streamPath);

        HashSet<String> list = new HashSet<>();

        ICsvListReader listReader = null;

        try {
            InputStreamReader insReader = new InputStreamReader(fileStream);

            listReader = new CsvListReader(insReader, CsvPreference.STANDARD_PREFERENCE);
            final CellProcessor[] processors = new CellProcessor[] { new NotNull(), // domain
            };

            List<Object> domainList;

            while ((domainList = listReader.read(processors)) != null) {
                String domain = (String) domainList.get(0);
                list.add(domain);
            }

            listReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Get the idOS API factory already configured with the set of credentials
     * @see #generateCredentials(String, String)
     * @param credentials a hashmap of credentials
     * @return the configured idOS API factory
     *
     */
    @Override
    public IdOSAPIFactory getIdOSAPIFactory(HashMap<String, String> credentials) {
        return new IdOSAPIFactory(credentials, configBean.getIDOSAPIURL(), !configBean.getUseSSLchecking());
    }

    /**
     * Merge a sequence of instances using a master header
     *
     * @param masterHeader common dataset header for all instances
     * @param instances sequence of instances
     * @return the merged data set
     */
    @Override
    public Instance mergeInstances(Instances masterHeader, Instance... instances) {
        Instance masterInstance = new DenseInstance(masterHeader.numAttributes());

        int attPos = 0;

        for (Instance instance : instances)
            for (int i = 0; i < (instance.numAttributes() - 2); i++)
                masterInstance.setValue(attPos++, instance.value(i));

        // check if there is any non-empty attribute
        // apply the all missing rule from mlmodels:
        boolean allMissing = true;

        for (int i = 0; i < masterInstance.numAttributes(); i++)
            if (!masterInstance.isMissing(i)) {
                allMissing = false;
                break;
            }

        double allMissingDval = allMissing ? 1 : 0;

        masterInstance.setValue(masterHeader.numAttributes() - 2, allMissingDval);

        return masterInstance;
    }

    /**
     * Merge attribute information from several dataset instances
     *
     * @param headers a sequence of all dataset headers
     *
     * @return the merged header
     */
    @Override
    public Instances mergeInstancesHeaders(Instances... headers) {

        ArrayList<Attribute> attList = new ArrayList<>();

        for (Instances header : headers)
            // add all but the isAllEmpty and class attribute
            for (int i = 0; i < (header.numAttributes() - 2); i++)
                attList.add(header.attribute(i));

        attList.addAll(createEmptyAndClassAttribute());

        Instances dataset = new Instances("FakeAndRealProfiles", attList, 0);
        dataset.setClassIndex(dataset.numAttributes() - 1);

        return dataset;
    }

    /**
     * Read a model from the filesystem.
     *
     * @param resourcePath path to the model file
     * @return the loaded model
     */
    @Override
    public IModel readModel(String resourcePath) {
        // read model file
        InputStream is = this.getClass().getResourceAsStream(resourcePath);
        try {
            return (IModel) new ObjectInputStream(is).readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create an HTML response header
     * @param modelName model name
     * @return the HTML header
     */
    public StringBuilder createResponseHeader(String modelName) {
        StringBuilder sb = new StringBuilder("<!DOCTYPE html><html><head><title>");
        sb.append(modelName);
        sb.append("</title></head><body><table border='1'>");
        return sb;
    }

    /**
     * Add a row in an HTML response for model result
     * @param response a stringbuilder response which will be appended to
     * @param rowName name of row
     * @param rowValue value in the row
     * @return the stringbuilder response object
     */
    public StringBuilder addResponseTableRow(StringBuilder response, String rowName, String rowValue) {
        response.append("<tr>");
        response.append("<td>");
        response.append(rowName);
        response.append("</td>");
        response.append("<td>");
        response.append(rowValue);
        response.append("</td>");
        response.append("</tr>");
        return response;
    }

    /**
     * Add a footer to the response
     * @param response stringbuilder object
     * @return the response object with the HTML footer appended to it
     */
    public String addResponseFooter(StringBuilder response) {
        response.append("</table></body></html>");
        return response.toString();
    }

    /**
     * Finds the index of the minority class for a dataset.
     *
     * @param dataset
     *            instances
     * @return index
     */
    @Override
    public int obtainMinorityClassIndex(Instances dataset) {
        if (dataset.classIndex() < 0)
            throw new RuntimeException("No class index defined on dataset!");

        AttributeStats classStats = dataset.attributeStats(dataset.classIndex());
        int[] classCounts = classStats.nominalCounts;

        int minorityClassIndex = 0;

        for (int i = 1; i < classCounts.length; i++)
            if (classCounts[i] < classCounts[minorityClassIndex])
                minorityClassIndex = i;

        return minorityClassIndex;
    }

    /**
     * Generic version of read unique facts
     *
     * @param streamPath path of the stream
     *
     * @return a list of facts
     */
    @Override
    public ArrayList<IFact> readFacts(String streamPath) {

        InputStream fileStream = this.getClass().getResourceAsStream(streamPath);

        ArrayList<IFact> facts = new ArrayList<>();
        ICsvListReader listReader = null;

        try {
            InputStreamReader insReader = new InputStreamReader(fileStream);

            listReader = new CsvListReader(insReader, CsvPreference.STANDARD_PREFERENCE);
            final CellProcessor[] processors = new CellProcessor[] { new NotNull(), // provider
                    new NotNull(), // fact
            };

            List<Object> providerFactList;

            while ((providerFactList = listReader.read(processors)) != null) {
                String provider = (String) providerFactList.get(0);
                String fact = (String) providerFactList.get(1);
                facts.add(new Fact(fact, provider));
            }

            listReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return facts;
    }

    /**
     * Reads the provider facts CSV file. Right now it contains a list of type provider,factName, in which only binary
     * or numeric facts are being used.
     *
     * @return a list with facts
     */
    @Override
    public ArrayList<IFact> readUniqueFactsNumericBinary() {
        return this.uniqueNumericBinaryFacts;
    }

    /**
     * Generates a dataset with 0 instances based on a list of facts
     *
     * @param facts
     *            the facts list
     * @return dataset
     */
    @Override
    public Instances generateDatasetHeader(ArrayList<IFact> facts) {

        // holds all the attributes of the relation
        ArrayList<Attribute> attList = new ArrayList<>();

        List<String> binaryValues = new ArrayList<>();
        binaryValues.add("0");
        binaryValues.add("1");

        // fill the attList with provider/facts
        for (IFact fact : facts) {
            String attName = fact.getProvider() + ":" + fact.getName();
            Attribute att = null;

            if (fact.getName().startsWith("is"))
                att = new Attribute(attName, binaryValues);
            else // a numeric attribute
                att = new Attribute(attName);
            attList.add(att);
        }

        attList.addAll(createEmptyAndClassAttribute());

        Instances dataset = new Instances("FakeAndRealProfiles", attList, 0);
        dataset.setClassIndex(dataset.numAttributes() - 1);

        return dataset;
    }

    /**
     * Creates the isEverythingEmpty attribute, which identifies an instance with all attributes missing.
     * Also creates the class attribute for an instance.
     *
     * @return an attribute list with the isEverythingEmpty and class attributes
     */
    @Override
    public ArrayList<Attribute> createEmptyAndClassAttribute() {

        ArrayList<Attribute> attList = new ArrayList<>();

        List<String> binaryValues = new ArrayList<>();
        binaryValues.add("0");
        binaryValues.add("1");

        Attribute allEmpty = new Attribute("skynet:isEverythingEmpty", binaryValues);
        attList.add(allEmpty);

        // add the supervision attribute:
        List<String> classValues = new ArrayList<>();
        classValues.add("fake");
        classValues.add("real");
        Attribute classAtt = new Attribute("class", classValues);
        attList.add(classAtt);

        return attList;
    }

    //    @Override
    //    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, ArrayList<IFact> facts, IUser user) {
    //
    //        // depending from where createInstance is called we may already have the profiles
    //        if ((user.getProfiles() == null) || (user.getProfiles().size() == 0))
    //            dao.obtainSingleUserProfiles(factory, user);
    //
    //        HashMap<IFact, String> userFacts = dao.obtainFactsForUser(factory, user);
    //
    //        Instance inst = new DenseInstance(dataset.numAttributes());
    //        inst.setDataset(dataset);
    //
    //        // go through the list of features, either filling them with user data
    //        // or a missing value if it doesnt exist.
    //
    //        int attIndex = 0;
    //
    //        for (IFact fact : facts) {
    //
    //            boolean userHasFact = userFacts.containsKey(fact);
    //
    //            if (userHasFact) {
    //
    //                // System.out.println(provider + ":" + fact + " => " + userProvFacts.get(provider).get(fact));
    //
    //                // the slicing starting from 2 is due to the php
    //                // serialize format
    //                // of using i: d: b:, etc
    //                String factValue = userFacts.get(fact);
    //
    //                int indexTotalCount = factValue.indexOf("total_count");
    //
    //                if (indexTotalCount != -1)
    //                    /*
    //                     * these instances have a serialized php array instead of a number for number of friends, because
    //                     * facebook changed its output. Sample of that format:
    //                     *
    //                     * a:2:{s:4:"data";a:0:{}s:7:"summary";a:1:{s:11:"total_count";i:900;}}
    //                     *
    //                     * What we want is the total count amount.
    //                     *
    //                     * Lets have fun with string slicing. Always remember the substring is a range of the form [a,b)
    //                     */
    //                    factValue = factValue.substring(indexTotalCount + 15, factValue.length() - 3);
    //                else // no shenanigans happened, just good ol' php serialize...
    //                    factValue = factValue.substring(2, factValue.length() - 1);
    //
    //                if (fact.getName().startsWith("is")) // this is a binary attribute
    //                    inst.setValue(attIndex, factValue);
    //                else // this is a numeric attribute
    //                    inst.setValue(attIndex, Double.parseDouble(factValue));
    //            } else // user either doesn't have provider or doesn't have fact
    //                inst.setValue(attIndex, weka.core.Utils.missingValue());
    //
    //            attIndex++;
    //        }
    //
    //        // add the isempty attribute:
    //        String isEmpty = userFacts.size() == 0 ? "1" : "0";
    //        inst.setValue(attIndex, isEmpty);
    //
    //        attIndex++;
    //
    //        if (user instanceof IAcceptRejectUser) {
    //            String supervision = ((IAcceptRejectUser) user).isReal() ? "real" : "fake";
    //            inst.setValue(attIndex, supervision);
    //        } else
    //            inst.setValue(attIndex, weka.core.Utils.missingValue());
    //
    //        return inst;
    //    }

    /**
     * Helper function to make a hard class prediction given class probabilities for an instance
     *
     * @param probabilities
     *            class probs for an instance
     * @return the class {0,1} that has maximum probability
     */
    @Override
    public int binaryArgMax(double[] probabilities) {
        return binaryArgMax(probabilities[0], probabilities[1]);
    }

    /**
     * Returns 0 if p0 is greater than p1
     *
     * @param p0 first probability
     * @param p1 second probability
     *
     * @return 0 if p0 is greater than p1
     */
    @Override
    public int binaryArgMax(double p0, double p1) {
        if (p1 > p0)
            return 1;
        return 0;
    }

    /**
     * Read an ARFF file from the filesystem. WARNING: This function will automatically set the class index to be the
     * last attribute.
     *
     * @param filePath path to the file
     * @return all instances in the file
     */
    @Override
    public Instances readARFF(String filePath) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            Instances data = new Instances(reader);
            data.setClassIndex(data.numAttributes() - 1);
            return data;
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the profile id of a selected provider given the list of profiles
     *
     * @param profiles the list of profiles
     * @param provider a provider name
     * @return the profile id for that provider
     */
    @Override
    public String getProfileId(ArrayList<IProfile> profiles, String provider) {
        for (IProfile prof : profiles)
            if (prof.getProvider().equals(provider))
                return prof.getProfileId();
        return null;
    }

    /**
     * Get the list of binary facts
     * @return the list of binary facts
     */
    @Override
    public ArrayList<IFact> getChecksBinaryFacts() {
        return checksBinaryFacts;
    }

    public void setChecksBinaryFacts(ArrayList<IFact> checksBinaryFacts) {
        this.checksBinaryFacts = checksBinaryFacts;
    }

    /**
     * Get the list of facebook numeric facts
     * @return the list of facebook numeric facts
     */
    @Override
    public ArrayList<IFact> getFacebookNumericFacts() {
        return facebookNumericFacts;
    }

    public void setFacebookNumericFacts(ArrayList<IFact> facebookNumericFacts) {
        this.facebookNumericFacts = facebookNumericFacts;
    }

    /**
     * Get the list of google binary facts
     * @return the list of google binary facts
     */
    @Override
    public ArrayList<IFact> getGoogleBinaryFacts() {
        return googleBinaryFacts;
    }

    public void setGoogleBinaryFacts(ArrayList<IFact> googleBinaryFacts) {
        this.googleBinaryFacts = googleBinaryFacts;
    }

    /**
     * Get the list of google numeric facts
     * @return the list of google numeric facts
     */
    @Override
    public ArrayList<IFact> getGoogleNumericFacts() {
        return googleNumericFacts;
    }

    public void setGoogleNumericFacts(ArrayList<IFact> googleNumericFacts) {
        this.googleNumericFacts = googleNumericFacts;
    }

    /**
     * Get the list of linkedin numeric facts
     * @return the list of linkedin numeric facts
     */
    @Override
    public ArrayList<IFact> getLinkedinNumericFacts() {
        return linkedinNumericFacts;
    }

    public void setLinkedinNumericFacts(ArrayList<IFact> linkedinNumericFacts) {
        this.linkedinNumericFacts = linkedinNumericFacts;
    }

    /**
     * Get the list of twitter numeric facts
     * @return the list of twitter numeric facts
     */
    @Override
    public ArrayList<IFact> getTwitterNumericFacts() {
        return twitterNumericFacts;
    }

    public void setTwitterNumericFacts(ArrayList<IFact> twitterNumericFacts) {
        this.twitterNumericFacts = twitterNumericFacts;
    }

    /**
     * Get the list of paypal binary facts
     * @return the list of paypal binary facts
     */
    @Override
    public ArrayList<IFact> getPaypalBinaryFacts() {
        return paypalBinaryFacts;
    }

    public void setPaypalBinaryFacts(ArrayList<IFact> paypalBinaryFacts) {
        this.paypalBinaryFacts = paypalBinaryFacts;
    }

}
