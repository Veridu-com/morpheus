package com.veridu.morpheus.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.idos.exceptions.InvalidToken;
import com.veridu.idos.exceptions.SDKException;
import com.veridu.idos.utils.Filter;
import com.veridu.idos.utils.IdOSAuthType;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.models.IModel;
import com.veridu.morpheus.interfaces.users.IProfile;
import com.veridu.morpheus.interfaces.users.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
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

    @Autowired
    public BeanUtils(BeanConfigurationManager configBean) {
        this.configBean = configBean;
    }

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

    private void preloadSerializedModels() {
        for (String modelName : Constants.MODEL_NAMES)
            this.serializedModels.put(modelName, this.readModel("/models/" + modelName));
    }

    public DriverManagerDataSource getFakeUsDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(configBean.getObjectStringProperty("db", "classname"));
        dataSource.setUrl(configBean.getObjectStringProperty("db", "url"));
        dataSource.setUsername(configBean.getObjectStringProperty("db", "username"));
        dataSource.setPassword(configBean.getObjectStringProperty("db", "password"));
        return dataSource;
    }

    @Override
    public boolean checkIfCandidatesExist(IdOSAPIFactory factory, IUser user, String attributeName) {
        try {

            factory.getCandidates().setAuthType(IdOSAuthType.HANDLER);
            JsonObject response = factory.getCandidates()
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

    @Override
    public HashMap<String, String> generateCredentials(String credentialPubKey, String username) {
        HashMap<String, String> credentials = new HashMap<>();
        credentials.put("credentialPublicKey", credentialPubKey);
        credentials.put("servicePrivateKey", configBean.getHandlerPrivateKey());
        credentials.put("servicePublicKey", configBean.getHandlerPublicKey());
        credentials.put("username", username);
        return credentials;
    }

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

    @Override
    public IdOSAPIFactory getIdOSAPIFactory(HashMap<String, String> credentials) {
        return new IdOSAPIFactory(credentials);
    }

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

    public StringBuilder createResponseHeader(String modelName) {
        StringBuilder sb = new StringBuilder("<!DOCTYPE html><html><head><title>");
        sb.append(modelName);
        sb.append("</title></head><body><table border='1'>");
        return sb;
    }

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

    public String addResponseFooter(StringBuilder response) {
        response.append("</table></body></html>");
        return response.toString();
    }

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

    @Override
    public ArrayList<IFact> readUniqueFactsNumericBinary() {
        return this.uniqueNumericBinaryFacts;
    }

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

    @Override
    public int binaryArgMax(double p0, double p1) {
        if (p1 > p0)
            return 1;
        return 0;
    }

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

    @Override
    public String getProfileId(ArrayList<IProfile> profiles, String provider) {
        for (IProfile prof : profiles)
            if (prof.getProvider().equals(provider))
                return prof.getProfileId();
        return null;
    }

    @Override
    public ArrayList<IFact> getChecksBinaryFacts() {
        return checksBinaryFacts;
    }

    public void setChecksBinaryFacts(ArrayList<IFact> checksBinaryFacts) {
        this.checksBinaryFacts = checksBinaryFacts;
    }

    @Override
    public ArrayList<IFact> getFacebookNumericFacts() {
        return facebookNumericFacts;
    }

    public void setFacebookNumericFacts(ArrayList<IFact> facebookNumericFacts) {
        this.facebookNumericFacts = facebookNumericFacts;
    }

    @Override
    public ArrayList<IFact> getGoogleBinaryFacts() {
        return googleBinaryFacts;
    }

    public void setGoogleBinaryFacts(ArrayList<IFact> googleBinaryFacts) {
        this.googleBinaryFacts = googleBinaryFacts;
    }

    @Override
    public ArrayList<IFact> getGoogleNumericFacts() {
        return googleNumericFacts;
    }

    public void setGoogleNumericFacts(ArrayList<IFact> googleNumericFacts) {
        this.googleNumericFacts = googleNumericFacts;
    }

    @Override
    public ArrayList<IFact> getLinkedinNumericFacts() {
        return linkedinNumericFacts;
    }

    public void setLinkedinNumericFacts(ArrayList<IFact> linkedinNumericFacts) {
        this.linkedinNumericFacts = linkedinNumericFacts;
    }

    @Override
    public ArrayList<IFact> getTwitterNumericFacts() {
        return twitterNumericFacts;
    }

    public void setTwitterNumericFacts(ArrayList<IFact> twitterNumericFacts) {
        this.twitterNumericFacts = twitterNumericFacts;
    }

    @Override
    public ArrayList<IFact> getPaypalBinaryFacts() {
        return paypalBinaryFacts;
    }

    public void setPaypalBinaryFacts(ArrayList<IFact> paypalBinaryFacts) {
        this.paypalBinaryFacts = paypalBinaryFacts;
    }

}
