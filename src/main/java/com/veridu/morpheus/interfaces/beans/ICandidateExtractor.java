/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.beans;

import com.veridu.morpheus.interfaces.facts.ICandidate;

import java.util.ArrayList;

public interface ICandidateExtractor {

    public ArrayList<ICandidate> extractCandidates(String userId);

}
