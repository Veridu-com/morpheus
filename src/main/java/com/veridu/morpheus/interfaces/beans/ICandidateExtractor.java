/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.beans;

import com.veridu.morpheus.interfaces.facts.ICandidate;

import java.util.ArrayList;

/**
 * Candidate extractor interface. Every candidate extractor should implement this interface.
 */
public interface ICandidateExtractor {

    /**
     * Return a list of candidates given a user id
     *
     * @param userId user id
     * @return list of candidates for a particular attribute
     */
    public ArrayList<ICandidate> extractCandidates(String userId);

}
