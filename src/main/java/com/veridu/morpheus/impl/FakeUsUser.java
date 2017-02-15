/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.facts.IAttribute;
import com.veridu.morpheus.interfaces.facts.ICandidate;
import com.veridu.morpheus.interfaces.users.IFakeUsUser;

import java.util.ArrayList;
import java.util.HashMap;

public class FakeUsUser extends User implements IFakeUsUser {

    private HashMap<IAttribute, ArrayList<ICandidate>> attributesMap = new HashMap<IAttribute, ArrayList<ICandidate>>();

    public FakeUsUser(String id) {
        super(id);
    }

    public FakeUsUser(String id, HashMap<IAttribute, ArrayList<ICandidate>> attributesMap) {
        super(id);
        this.attributesMap = attributesMap;
    }

    @Override
    public HashMap<IAttribute, ArrayList<ICandidate>> getAttributesMap() {
        return this.attributesMap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("This is user " + this.getId() + " attributes/candidates:\n");
        for (IAttribute att : this.attributesMap.keySet()) {
            ArrayList<ICandidate> candidates = this.attributesMap.get(att);
            for (ICandidate cand : candidates)
                sb.append(att.getName() + ": " + cand.getValue() + " => " + cand.isReal() + "\n");
        }
        return sb.toString();
    }

    @Override
    public void setAttributesMap(HashMap<IAttribute, ArrayList<ICandidate>> map) {
        this.attributesMap = map;
    }

}
