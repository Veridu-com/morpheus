package com.veridu.morpheus.features;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Attribute;
import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IFeatureExtractor;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.ICandidate;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.users.IFakeUsUser;
import com.veridu.morpheus.interfaces.users.IUser;
import com.veridu.morpheus.utils.LocalUtils;
import com.veridu.morpheus.utils.RulesEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
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
@Component("birthDayExtractor")
@Scope("singleton")
public class BeanBirthDayFeatureExtractor implements IFeatureExtractor {

    private static final boolean DEBUG = false;

    private IFeatureExtractor facebookFeatureExtractor;

    private IFeatureExtractor facebookBirthdayExtractor;

    private Instances facebookHeader;

    private Instances facebookBirthdayHeader;

    private IDataSource dataSource;

    private IUtils utils;

    @Autowired
    public BeanBirthDayFeatureExtractor(@Qualifier("facebookExtractor") IFeatureExtractor facebookFeatureExtractor,
            @Qualifier("facebookBirthdayFactsExtractor") IFeatureExtractor facebookBirthdayExtractor, IUtils utils,
            @Qualifier("idosSQL") IDataSource dataSource) {
        this.facebookFeatureExtractor = facebookFeatureExtractor;
        this.facebookBirthdayExtractor = facebookBirthdayExtractor;
        this.utils = utils;
        this.dataSource = dataSource;
    }

    private ArrayList<IFact> factList = new ArrayList<>();

    private static final String[] providers = { Constants.CPR_PROVIDER_NAME, Constants.FACEBOOK_PROVIDER_NAME,
            Constants.LINKEDIN_PROVIDER_NAME, Constants.PAYPAL_PROVIDER_NAME, Constants.PERSONAL_PROVIDER_NAME,
            Constants.SPOTIFY_PROVIDER_NAME, Constants.YAHOO_PROVIDER_NAME };

    private static final String bDayFactName = "birthDay";

    private static final ArrayList<IFact> providerFacts = LocalUtils.generateProvidersFact(providers, bDayFactName);

    private static final String myProviderName = "birth-day-features";

    private static RulesEngine rulesEngine = new RulesEngine(bDayFactName, myProviderName, providers, true);

    @PostConstruct
    private void init() {
        this.factList.addAll(this.facebookFeatureExtractor.obtainFactList());
        this.factList.addAll(rulesEngine.getFactList());
        this.facebookHeader = this.utils.generateDatasetHeader(this.facebookFeatureExtractor.obtainFactList());
    }

    @Override
    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, IUser user) {

        Instance inst = new DenseInstance(dataset.numAttributes());
        inst.setDataset(dataset);

        int attPosCounter = 0;

        Instance basicFacebookInst = this.facebookFeatureExtractor.createInstance(factory, this.facebookHeader, user);
        for (int i = 0; i < (basicFacebookInst.numAttributes() - 2); i++)
            inst.setValue(attPosCounter++, basicFacebookInst.value(i));

        // Instance fbkInst = this.facebookBirthdayExtractor.createInstance(this.facebookBirthdayHeader, user);
        // for (int i = 0; i < (fbkInst.numAttributes() - 2); i++)
        // inst.setValue(attPosCounter++, fbkInst.value(i));

        // get birth facts:
        HashMap<IFact, String> userFacts = dataSource.obtainSpecificFactForUser(factory, user, "*birth*");
        ArrayList<String> factValues = new ArrayList<>();

        for (IFact provFact : providerFacts)
            if (userFacts.containsKey(provFact))
                factValues.add(userFacts.get(provFact));
            else
                factValues.add("0");

        // apply rules
        double[] values = rulesEngine.applyRules(factValues);
        for (int i = 0; i < values.length; i++)
            inst.setValue(attPosCounter++, values[i]);

        // apply the all missing rule from mlmodels:
        LocalUtils.setBinaryValueAtPos(inst, inst.numAttributes() - 2, LocalUtils.allAttributesMissing(inst));

        // figure out what is the supervision if we're creating instances for training
        if (user instanceof IFakeUsUser) {
            IFakeUsUser fuser = (IFakeUsUser) user;
            ArrayList<ICandidate> candidates = fuser.getAttributesMap().get(new Attribute(bDayFactName));

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
            System.out.println("Birth day feature extractor for user " + user.getId());
            System.out.println("Providers array:");
            System.out.println(Arrays.toString(providers));
            System.out.println("Birth day list (follows order of providers):");
            System.out.println(Arrays.toString(factValues.toArray()));
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
