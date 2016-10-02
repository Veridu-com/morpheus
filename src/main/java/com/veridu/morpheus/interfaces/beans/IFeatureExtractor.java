package com.veridu.morpheus.interfaces.beans;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.users.IUser;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;

public interface IFeatureExtractor {

    public Instance createInstance(IdOSAPIFactory factory, Instances dataset, IUser user);

    public ArrayList<IFact> obtainFactList();

}
