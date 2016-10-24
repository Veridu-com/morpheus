package com.veridu.morpheus.features;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.interfaces.beans.IDataSource;
import com.veridu.morpheus.interfaces.beans.IFeatureExtractor;
import com.veridu.morpheus.interfaces.beans.IMongoDataSource;
import com.veridu.morpheus.interfaces.beans.IUtils;
import com.veridu.morpheus.interfaces.facts.IFact;
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
import java.util.Date;
import java.util.HashMap;

/**
 * Created by cassio on 10/3/16.
 */

@Component("facebookBirthdayFactsExtractor")
@Scope("singleton")
public class BeanFacebookBirthdayFactsExtractor implements IFeatureExtractor {

    private IDataSource dataSource;

    private IUtils utils;

    private IMongoDataSource mongo;

    private ArrayList<IFact> numericFacts;
    private ArrayList<IFact> factList = new ArrayList<>();

    private static final String myProviderName = "facebookBirthdayFacts";

    @Autowired
    public BeanFacebookBirthdayFactsExtractor(IDataSource dataSource, IUtils utils, IMongoDataSource mongo) {
        this.dataSource = dataSource;
        this.utils = utils;
        this.mongo = mongo;
    }

    @PostConstruct
    private void init() {
        // base fact list lives on the csv file

        // get the base list of facts we're interested in
        this.numericFacts = this.utils.readFacts("/csvs/birthyear-facts.csv");
        factList.addAll(this.numericFacts);

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
    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, IUser user) {

        Instance inst = new DenseInstance(dataset.numAttributes());
        inst.setDataset(dataset);

        if (1 == 0) { // FIXME ignoring facebook for now

            HashMap<IFact, String> numericFactsMap = dataSource.obtainSpecificFactForUser(factory, user, "*birth*");

            int attPos = 0;

            for (IFact fact : this.numericFacts) {
                if (numericFactsMap.containsKey(fact)) {
                    Integer value = Integer.parseInt(numericFactsMap.get(fact));
                    inst.setValue(attPos, value);
                }
                attPos++;
            }

            // mongodb features:
            int fbkPostsBirthday = 0;
            int fbkTagsBirthday = 0;
            int fbkStatusBirthday = 0;
            int fbkPhotosBirthday = 0;
            int fbkLikesBirthday = 0;
            int fbkEventsBirthday = 0;

            Boolean fbkEmailMatchFirstName = null;
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

            inst.setValue(attPos++, fbkPostsBirthday);
            inst.setValue(attPos++, fbkTagsBirthday);
            inst.setValue(attPos++, fbkStatusBirthday);
            inst.setValue(attPos++, fbkPhotosBirthday);
            inst.setValue(attPos++, fbkLikesBirthday);
            inst.setValue(attPos++, fbkEventsBirthday);

            if (fbkEmailMatchFirstName == null)
                inst.setValue(attPos++, Utils.missingValue());
            else if (fbkEmailMatchFirstName)
                inst.setValue(attPos++, "1");
            else
                inst.setValue(attPos++, "0");
        }
        return inst;
    }

    @Override
    public ArrayList<IFact> obtainFactList() {
        return this.factList;
    }

}
