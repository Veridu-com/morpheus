package com.veridu.morpheus.features;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Attribute;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IFeatureExtractor;
import com.veridu.morpheus.interfaces.beans.IMongoDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.ICandidate;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.users.IFakeUsUser;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.LocalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by cassio on 10/3/16.
 */
@Component("lastNameExtractor")
public class BeanLastNameFeatureExtractor implements IFeatureExtractor {

    private IDataSource dataSource;

    private IUtils utils;

    private IMongoDataSource mongo;

    private IFeatureExtractor facebookFeatureExtractor;

    private IFeatureExtractor linkedinFeatureExtractor;

    private IFeatureExtractor twitterFeatureExtractor;

    private IFeatureExtractor paypalFeatureExtractor;

    private Instances facebookFeaturesHeader;
    private Instances linkedinFeaturesHeader;
    private Instances twitterFeaturesHeader;
    private Instances paypalFeaturesHeader;

    @Autowired
    public BeanLastNameFeatureExtractor(IDataSource dataSource, IUtils utils, IMongoDataSource mongo,
            @Qualifier("facebookExtractor") IFeatureExtractor facebookFeatureExtractor,
            @Qualifier("linkedinExtractor") IFeatureExtractor linkedinFeatureExtractor,
            @Qualifier("twitterExtractor") IFeatureExtractor twitterFeatureExtractor,
            @Qualifier("paypalExtractor") IFeatureExtractor paypalFeatureExtractor) {
        this.dataSource = dataSource;
        this.utils = utils;
        this.mongo = mongo;
        this.facebookFeatureExtractor = facebookFeatureExtractor;
        this.linkedinFeatureExtractor = linkedinFeatureExtractor;
        this.twitterFeatureExtractor = twitterFeatureExtractor;
        this.paypalFeatureExtractor = paypalFeatureExtractor;
    }

    private ArrayList<IFact> factList;

    private static final String myProviderName = "last-name-features";

    private static final String lastNameFactName = "lastName";

    private static final ArrayList<IFact> providerFacts = LocalUtils
            .generateProvidersFact(Constants.PROVIDERS_NAMES, lastNameFactName);

    private static final double LEVENSHTEIN_SIMILARITY_THRESHOLD = 0.6;

    private static final boolean DEBUG = false;

    private int ruleIndexStart = -1;
    private int ruleIndexStop = -1; // last index exclusive

    // we store the following so we can execute the rules faster than doing a lot of string parsing
    private ArrayList<Integer> ruleOperand1 = new ArrayList<>(); // index of the first provider of a rule
    private ArrayList<Integer> ruleOperand2 = new ArrayList<>(); // index of the second provider of a rule
    private ArrayList<String> opCodes = new ArrayList<>(); // operator of the rule, e.g., =, <, >, <=, >=

    @PostConstruct
    private void init() {
        this.factList = new ArrayList<>();

        ruleIndexStart = 0;
        ruleIndexStop = ruleIndexStart;

        // now dynamically create rules that match birth year on all combinations of our providers
        for (int i = 0; i < (providerFacts.size() - 1); i++)
            for (int j = i + 1; j < providerFacts.size(); j++) {
                IFact fct = new Fact(
                        "is" + providerFacts.get(i).getProvider() + "LastNameMatches" + providerFacts.get(j)
                                .getProvider() + "LastName", myProviderName);
                factList.add(fct);
                ruleOperand1.add(i);
                ruleOperand2.add(j);
                opCodes.add("=");
                ruleIndexStop++;
            }

        ArrayList<IFact> fbkFacts = this.facebookFeatureExtractor.obtainFactList();
        ArrayList<IFact> linkedinFacts = this.linkedinFeatureExtractor.obtainFactList();
        ArrayList<IFact> twitterFacts = this.twitterFeatureExtractor.obtainFactList();
        ArrayList<IFact> paypalFacts = this.paypalFeatureExtractor.obtainFactList();

        // add provider facts
        factList.addAll(fbkFacts);
        factList.addAll(linkedinFacts);
        factList.addAll(twitterFacts);
        factList.addAll(paypalFacts);

        this.facebookFeaturesHeader = this.utils.generateDatasetHeader(fbkFacts);
        this.linkedinFeaturesHeader = this.utils.generateDatasetHeader(linkedinFacts);
        this.twitterFeaturesHeader = this.utils.generateDatasetHeader(twitterFacts);
        this.paypalFeaturesHeader = this.utils.generateDatasetHeader(paypalFacts);
    }

    @Override
    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, IUser user) {
        // System.err.println("======> going to extract features for user " + user.getId());

        HashMap<IFact, String> lastNameFacts = dataSource.obtainSpecificFactForUser(factory, user, lastNameFactName);

        String[] lastNameList = new String[providerFacts.size()];

        for (int i = 0; i < providerFacts.size(); i++) {
            IFact pfact = providerFacts.get(i);
            if (lastNameFacts.containsKey(pfact))
                lastNameList[i] = lastNameFacts.get(pfact);
            else
                lastNameList[i] = null;
        }

        Instance inst = new DenseInstance(dataset.numAttributes());
        inst.setDataset(dataset);

        int attPosCounter = 0;

        // apply the matching rules we've dynamically created
        for (int i = ruleIndexStart, j = 0; i < ruleIndexStop; i++, j++) {
            int op1 = ruleOperand1.get(j);
            int op2 = ruleOperand2.get(j);
            String opCode = opCodes.get(j);
            if ((lastNameList[op1] != null) && (lastNameList[op2] != null)) // otherwise it's just missing data
                LocalUtils.applyNameSimRule(inst, i, opCode, lastNameList[op1], lastNameList[op2],
                        LEVENSHTEIN_SIMILARITY_THRESHOLD);
            attPosCounter++;
        }

        // facebook feature extraction
        Instance fbkInst = facebookFeatureExtractor.createInstance(factory, this.facebookFeaturesHeader, user);
        for (int i = 0; i < (fbkInst.numAttributes() - 2); i++)
            inst.setValue(attPosCounter++, fbkInst.value(i));

        // linkedin feature extraction
        Instance linkedinInst = linkedinFeatureExtractor.createInstance(factory, this.linkedinFeaturesHeader, user);
        for (int i = 0; i < (linkedinInst.numAttributes() - 2); i++)
            inst.setValue(attPosCounter++, linkedinInst.value(i));

        // twitter feature extraction
        Instance twitterInst = twitterFeatureExtractor.createInstance(factory, this.twitterFeaturesHeader, user);
        for (int i = 0; i < (twitterInst.numAttributes() - 2); i++)
            inst.setValue(attPosCounter++, twitterInst.value(i));

        // paypal feature extraction
        Instance paypalInst = paypalFeatureExtractor.createInstance(factory, this.paypalFeaturesHeader, user);
        for (int i = 0; i < (paypalInst.numAttributes() - 2); i++)
            inst.setValue(attPosCounter++, paypalInst.value(i));

        // apply the all missing rule from mlmodels:
        boolean allMissing = true;
        for (int i = 0; i < inst.numAttributes(); i++)
            if (!inst.isMissing(i)) {
                allMissing = false;
                break;
            }

        LocalUtils.setBinaryValueAtPos(inst, inst.numAttributes() - 2, allMissing);

        // figure out what is the supervision if we're creating instances for training
        if (user instanceof IFakeUsUser) {
            IFakeUsUser fuser = (IFakeUsUser) user;
            ArrayList<ICandidate> candidates = fuser.getAttributesMap().get(new Attribute(lastNameFactName));

            // TODO: later we should build this based on the candidates. for now we'll assume all candidates are
            // either real or fake.
            boolean isReal = false;
            if (candidates != null)
                isReal = candidates.get(0).isReal();
            String sup = isReal ? "real" : "fake";
            inst.setClassValue(sup);
        } else
            inst.setClassMissing();

        if (DEBUG) {
            System.out.println("--------------------------");
            System.out.println("Last name feature extractor for user " + user.getId());
            System.out.println("Providers array:");
            System.out.println(Arrays.toString(Constants.PROVIDERS_NAMES));
            System.out.println("Last name list (follows order of providers):");
            System.out.println(Arrays.toString(lastNameList));
            System.out.println("Resulting instance:");
            System.out.println(inst);
            System.out.println("--------------------------");
        }

        return inst;
    }

    @Override
    public ArrayList<IFact> obtainFactList() {
        return this.factList;
    }

}

