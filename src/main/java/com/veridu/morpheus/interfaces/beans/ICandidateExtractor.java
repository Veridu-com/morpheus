package com.veridu.morpheus.interfaces.beans;

import com.veridu.morpheus.interfaces.facts.ICandidate;

import java.util.ArrayList;

public interface ICandidateExtractor {

    public ArrayList<ICandidate> extractCandidates(String userId);

}
