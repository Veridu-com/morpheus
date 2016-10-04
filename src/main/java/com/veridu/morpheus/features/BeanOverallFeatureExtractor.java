package com.veridu.morpheus.features;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Attribute;
import com.veridu.morpheus.interfaces.beans.IFeatureExtractor;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.ICandidate;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.users.IFakeUsUser;
import com.veridu.morpheus.interfaces.users.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import weka.core.Instance;
import weka.core.Instances;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

/**
 * Created by cassio on 10/3/16.
 */
@Component("overallExtractor")
public class BeanOverallFeatureExtractor implements IFeatureExtractor {

    private static final boolean DEBUG = false;

    private IFeatureExtractor checkExtractor;

    private IFeatureExtractor googleExtractor;

    private IFeatureExtractor facebookExtractor;

    private IFeatureExtractor linkedinExtractor;

    private IFeatureExtractor twitterExtractor;

    private IUtils utils;

    private ArrayList<IFact> facts = new ArrayList<>();

    private Instances checkHeader;
    private Instances facebookHeader;
    private Instances googleHeader;
    private Instances linkedinHeader;
    private Instances twitterHeader;

    @Autowired
    public BeanOverallFeatureExtractor(@Qualifier("checkExtractor") IFeatureExtractor checkExtractor,
            @Qualifier("googleExtractor") IFeatureExtractor googleExtractor,
            @Qualifier("facebookExtractor") IFeatureExtractor facebookExtractor,
            @Qualifier("linkedinExtractor") IFeatureExtractor linkedinExtractor,
            @Qualifier("twitterExtractor") IFeatureExtractor twitterExtractor, IUtils utils) {
        this.checkExtractor = checkExtractor;
        this.googleExtractor = googleExtractor;
        this.facebookExtractor = facebookExtractor;
        this.linkedinExtractor = linkedinExtractor;
        this.twitterExtractor = twitterExtractor;
        this.utils = utils;
    }

    @PostConstruct
    public void init() {
        this.facts.addAll(checkExtractor.obtainFactList());
        this.facts.addAll(facebookExtractor.obtainFactList());
        this.facts.addAll(googleExtractor.obtainFactList());
        this.facts.addAll(linkedinExtractor.obtainFactList());
        this.facts.addAll(twitterExtractor.obtainFactList());

        this.checkHeader = this.utils.generateDatasetHeader(this.checkExtractor.obtainFactList());
        this.facebookHeader = this.utils.generateDatasetHeader(this.facebookExtractor.obtainFactList());
        this.googleHeader = this.utils.generateDatasetHeader(this.googleExtractor.obtainFactList());
        this.linkedinHeader = this.utils.generateDatasetHeader(this.linkedinExtractor.obtainFactList());
        this.twitterHeader = this.utils.generateDatasetHeader(this.twitterExtractor.obtainFactList());
    }

    @Override
    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, IUser user) {

        Instance checkInst = this.checkExtractor.createInstance(factory, checkHeader, user);
        Instance facebookInst = this.facebookExtractor.createInstance(factory, facebookHeader, user);
        Instance googleInst = this.googleExtractor.createInstance(factory, googleHeader, user);
        Instance linkedinInst = this.linkedinExtractor.createInstance(factory, linkedinHeader, user);
        Instance twitterInst = this.twitterExtractor.createInstance(factory, twitterHeader, user);

        if (DEBUG) {
            System.out.println("----------------------------");
            System.out.println("individual instances generated for overall:");
            System.out.println(checkInst);
            System.out.println(facebookInst);
            System.out.println(googleInst);
            System.out.println(linkedinInst);
            System.out.println(twitterInst);
        }

        Instance mergedInstance = this.utils
                .mergeInstances(dataset, checkInst, facebookInst, googleInst, linkedinInst, twitterInst);

        mergedInstance.setDataset(dataset);

        // figure out what is the supervision if we're creating instances for training
        if (user instanceof IFakeUsUser) {

            IFakeUsUser fuser = (IFakeUsUser) user;

            ArrayList<ICandidate> candidates = fuser.getAttributesMap().get(new Attribute("overall"));

            if (candidates == null)
                mergedInstance.setClassValue("fake");
            else {
                boolean isReal = candidates.get(0).isReal();
                String sup = isReal ? "real" : "fake";
                mergedInstance.setClassValue(sup);
            }
        } else
            mergedInstance.setClassMissing();

        if (DEBUG) {
            System.out.println("--------------------------");
            System.out.println(String.format("Resulting instance for user %s", user.getId()));
            System.out.println(mergedInstance);
            System.out.println("--------------------------");
        }

        return mergedInstance;
    }

    @Override
    public ArrayList<IFact> obtainFactList() {
        return this.facts;
    }

}

