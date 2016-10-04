package com.veridu.morpheus.features;

import com.google.gson.JsonObject;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by cassio on 10/3/16.
 */

@Scope("singleton")
@Component("birthYearExtractor")
public class BeanBirthYearFeatureExtractor implements IFeatureExtractor {

    private static final boolean DEBUG = false;

    private IDataSource dataSource;

    private IUtils utils;

    private IMongoDataSource mongo;

    @Autowired
    public BeanBirthYearFeatureExtractor(IDataSource dataSource, IUtils utils, IMongoDataSource mongo) {
        this.dataSource = dataSource;
        this.utils = utils;
        this.mongo = mongo;
    }

    // save a cache of the facts list so we don't have to dynamically generate the rules
    // every time.
    private ArrayList<IFact> factList;

    // this indicates the index that the dynamic rules should start on, i.e., after the base facts
    // stored on the csv file.
    private int ruleIndexStart = -1;
    private int ruleIndexStop = -1; // last index exclusive

    // we store the following so we can execute the rules faster than doing a lot of string parsing
    private ArrayList<Integer> ruleOperand1 = new ArrayList<>(); // index of the first provider of a rule
    private ArrayList<Integer> ruleOperand2 = new ArrayList<>(); // index of the second provider of a rule
    private ArrayList<String> opCodes = new ArrayList<>(); // operator of the rule, e.g., =, <, >, <=, >=

    // *** VERY IMPORTANT NOTICE ***
    //
    // Observe very carefully the order of the providers, as this is used in multiple places ON THIS SOURCE FILE.
    // The index of the provider is used to apply the rules very quickly, without having to do
    // string parsing or searches or even hashing.
    //
    // ALWAYS ADD A NEW PROVIDER AT THE END AND OBSERVE THAT ORDER EVERYWHERE ELSE. THANK YOU.
    //
    // *** END OF VERY IMPORTANT NOTICE ***
    private static final String[] providers = { Constants.CPR_PROVIDER_NAME, Constants.FACEBOOK_PROVIDER_NAME,
            Constants.LINKEDIN_PROVIDER_NAME, Constants.PAYPAL_PROVIDER_NAME, Constants.PERSONAL_PROVIDER_NAME,
            Constants.SPOTIFY_PROVIDER_NAME, Constants.YAHOO_PROVIDER_NAME };

    private static final String bYearFactName = "birthYear";

    private static final IFact cprBirthYearFact = new Fact(bYearFactName, providers[0]);
    private static final IFact fbkBirthYearFact = new Fact(bYearFactName, providers[1]);
    private static final IFact linBirthYearFact = new Fact(bYearFactName, providers[2]);
    private static final IFact payBirthYearFact = new Fact(bYearFactName, providers[3]);
    private static final IFact perBirthYearFact = new Fact(bYearFactName, providers[4]);
    private static final IFact spoBirthYearFact = new Fact(bYearFactName, providers[5]);
    private static final IFact yahBirthYearFact = new Fact(bYearFactName, providers[6]);

    private static final String myProviderName = "birth-year-features";

    @PostConstruct
    private void init() {
        // base fact list lives on the csv file

        // get the base list of facts we're interested in
        factList = new ArrayList<>(this.utils.readFacts("/csvs/birthyear-facts.csv"));

        ruleIndexStart = factList.size();
        ruleIndexStop = ruleIndexStart;

        // now dynamically create rules that match birth year on all combinations of our providers
        for (int i = 0; i < (providers.length - 1); i++)
            for (int j = i + 1; j < providers.length; j++) {
                IFact fct = new Fact("is" + providers[i] + "BirthYearMatches" + providers[j] + "BirthYear",
                        myProviderName);
                factList.add(fct);
                ruleOperand1.add(i);
                ruleOperand2.add(j);
                opCodes.add("=");
                ruleIndexStop++;
            }

        // add mongo facts
        factList.add(new Fact("numFacebookPostsOnFbkBirthday", myProviderName));
        factList.add(new Fact("numFacebookTagsOnFbkBirthday", myProviderName));
        factList.add(new Fact("numFacebookStatusesOnFbkBirthday", myProviderName));
        factList.add(new Fact("numFacebookPhotosOnFbkBirthday", myProviderName));
        factList.add(new Fact("numFacebookLikesOnFbkBirthday", myProviderName));
        factList.add(new Fact("numFacebookEventsOnFbkBirthday", myProviderName));
        factList.add(new Fact("isFacebookFirstNameMatchEmail", myProviderName));
    }

    @Override
    public ArrayList<IFact> obtainFactList() {
        return this.factList;
    }

    private static void setBinaryValueAtPos(Instance inst, final int pos, boolean comparison) {
        if (comparison)
            inst.setValue(pos, "1");
        else
            inst.setValue(pos, "0");
    }

    private static void applyRule(Instance inst, final int pos, final String opCode, final int value1,
            final int value2) {
        switch (opCode) {
        case "=":
            setBinaryValueAtPos(inst, pos, value1 == value2);
            break;
        }
    }

    @Override
    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, IUser user) {
        ArrayList<IFact> facts = this.obtainFactList();

        HashMap<IFact, String> userFacts = dataSource.obtainSpecificFactForUser(factory, user, "*birth*");

        // MAKE SURE THE ORDER YOU INSERT BIRTH YEARS IN THIS ARRAY FOLLOWS
        // THE SAME ORDER THE PROVIDERS ARE DEFINED AT THE TOP OF THIS FILE.
        // THANK YOU AND HAVE A NICE DAY.
        int[] birthYears = { -1, -1, -1, -1, -1, -1, -1 };

        if (userFacts.containsKey(cprBirthYearFact))
            birthYears[0] = Integer.parseInt(userFacts.get(cprBirthYearFact));
        if (userFacts.containsKey(fbkBirthYearFact))
            birthYears[1] = Integer.parseInt(userFacts.get(fbkBirthYearFact));
        if (userFacts.containsKey(linBirthYearFact))
            birthYears[2] = Integer.parseInt(userFacts.get(linBirthYearFact));
        if (userFacts.containsKey(payBirthYearFact))
            birthYears[3] = Integer.parseInt(userFacts.get(payBirthYearFact));
        if (userFacts.containsKey(perBirthYearFact))
            birthYears[4] = Integer.parseInt(userFacts.get(perBirthYearFact));
        if (userFacts.containsKey(spoBirthYearFact))
            birthYears[5] = Integer.parseInt(userFacts.get(spoBirthYearFact));
        if (userFacts.containsKey(yahBirthYearFact))
            birthYears[6] = Integer.parseInt(userFacts.get(yahBirthYearFact));

        Instance inst = new DenseInstance(dataset.numAttributes());
        inst.setDataset(dataset);

        int attPosCounter = 0;

        // lets get the base facts first. this is not optional, it has to be in this order.
        for (int i = 0; i < ruleIndexStart; i++) {
            IFact fct = facts.get(i);
            if (userFacts.containsKey(fct))
                inst.setValue(i, Double.parseDouble(userFacts.get(fct)));
            attPosCounter++;
        }

        // apply the matching rules we've dynamically created
        for (int i = ruleIndexStart, j = 0; i < ruleIndexStop; i++, j++) {
            int op1 = ruleOperand1.get(j);
            int op2 = ruleOperand2.get(j);
            String opCode = opCodes.get(j);
            if ((birthYears[op1] > 0) && (birthYears[op2] > 0)) // otherwise it's just missing data
                applyRule(inst, i, opCode, birthYears[op1], birthYears[op2]);
            attPosCounter++;
        }

        // mongodb features:

        int fbkPostsBirthday = 0;
        int fbkTagsBirthday = 0;
        int fbkStatusBirthday = 0;
        int fbkPhotosBirthday = 0;
        int fbkLikesBirthday = 0;
        int fbkEventsBirthday = 0;
        Boolean fbkEmailMatchFirstName = null;

        JsonObject facebookProfile = mongo.getFacebookProfile(factory, user);

        if (facebookProfile != null) {
            Date facebookBirthday = this.mongo.getFacebookBirthday(factory, user);

            if (facebookBirthday != null) {
                fbkPostsBirthday = this.mongo.getNumberOfFacebookPostsOnBirthday(factory, user, facebookBirthday);
                fbkTagsBirthday = this.mongo.getNumberOfFacebookTagsOnBirthday(factory, user, facebookBirthday);
                fbkStatusBirthday = this.mongo.getNumberOfFacebookStatusOnBirthday(factory, user, facebookBirthday);
                fbkPhotosBirthday = this.mongo.getNumberOfFacebookPhotosOnBirthday(factory, user, facebookBirthday);
                fbkLikesBirthday = this.mongo.getNumberOfFacebookLikesOnBirthday(factory, user, facebookBirthday);
                fbkEventsBirthday = this.mongo.getNumberOfFacebookEventsOnBirthday(factory, user, facebookBirthday);
            }

            fbkEmailMatchFirstName = this.mongo.doesFacebookFirstNameMatchEmail(factory, user);
        }

        inst.setValue(attPosCounter++, fbkPostsBirthday);
        inst.setValue(attPosCounter++, fbkTagsBirthday);
        inst.setValue(attPosCounter++, fbkStatusBirthday);
        inst.setValue(attPosCounter++, fbkPhotosBirthday);
        inst.setValue(attPosCounter++, fbkLikesBirthday);
        inst.setValue(attPosCounter++, fbkEventsBirthday);

        if (fbkEmailMatchFirstName == null)
            inst.setValue(attPosCounter++, Utils.missingValue());
        else if (fbkEmailMatchFirstName)
            inst.setValue(attPosCounter++, "1");
        else
            inst.setValue(attPosCounter++, "0");

        // apply the all missing rule from mlmodels:
        boolean allMissing = true;
        for (int i = 0; i < inst.numAttributes(); i++)
            if (!inst.isMissing(i)) {
                allMissing = false;
                break;
            }

        setBinaryValueAtPos(inst, inst.numAttributes() - 2, allMissing);

        // figure out what is the supervision if we're creating instances for training
        if (user instanceof IFakeUsUser) {
            IFakeUsUser fuser = (IFakeUsUser) user;
            ArrayList<ICandidate> candidates = fuser.getAttributesMap().get(new Attribute(bYearFactName));

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
            System.out.println("Birth year feature extractor for user " + user.getId());
            System.out.println("Providers array:");
            System.out.println(Arrays.toString(providers));
            System.out.println("Birth years (follows order of providers):");
            System.out.println(Arrays.toString(birthYears));
            System.out.println("Resulting instance:");
            System.out.println(inst);
            System.out.println("--------------------------");
        }

        return inst;
    }

}
