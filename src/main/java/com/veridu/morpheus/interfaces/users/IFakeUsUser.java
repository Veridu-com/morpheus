package com.veridu.morpheus.interfaces.users;

import com.veridu.morpheus.interfaces.facts.IAttribute;
import com.veridu.morpheus.interfaces.facts.ICandidate;

import java.util.ArrayList;
import java.util.HashMap;

public interface IFakeUsUser extends IUser {

    public HashMap<IAttribute, ArrayList<ICandidate>> getAttributesMap();

    public void setAttributesMap(HashMap<IAttribute, ArrayList<ICandidate>> map);

}
