/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

/**
 * Return object for a cross validation run
 */
public class CVEvalReturn {

    public double[] pred; // prediction labels
    public double[] sup; // supervision labels

    /**
     * Constructor
     * @param pred predictions array
     * @param sup true class values array (supervision)
     */
    public CVEvalReturn(double[] pred, double[] sup) {
        this.pred = pred;
        this.sup = sup;
    }

}
