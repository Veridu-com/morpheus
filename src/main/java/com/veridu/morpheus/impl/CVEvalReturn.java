/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

public class CVEvalReturn {

    public double[] pred; // prediction labels
    public double[] sup; // supervision labels

    public CVEvalReturn(double[] pred, double[] sup) {
        this.pred = pred;
        this.sup = sup;
    }

}
