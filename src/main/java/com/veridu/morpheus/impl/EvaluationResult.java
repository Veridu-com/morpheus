/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.utils.IEvaluationResult;

public class EvaluationResult implements IEvaluationResult {

    public double threshold;
    public double falsePositives = 0;
    public double truePositives = 0;
    public double falseNegatives = 0;
    public double trueNegatives = 0;

    /**
     * Computes the False Positive Rate (FPR)
     * FPR = FP / (FP + TN)
     *
     * @return fpr value in [0,100]
     */
    @Override
    public double getFalsePositiveRate() {
        double fpPlusTn = falsePositives + trueNegatives;
        if ((fpPlusTn) > 0)
            return (100.0 * falsePositives) / (fpPlusTn);
        return 0;
    }

    /**
     * Computes the True Positive Rate (TPR), aka Recall
     * Recall = TP / (TP + FN)
     *
     * @return recall value in [0,100]
     */
    @Override
    public double getRecall() {
        double tpPlusFn = truePositives + falseNegatives;
        if (tpPlusFn > 0)
            return (100.0 * truePositives) / (tpPlusFn);
        return 0;
    }

    /**
     * Computes the Precision, or Positive Predictive Value (PPV)
     * Precision = TP / (TP + FP)
     *
     * @return precision value in [0,100]
     */
    @Override
    public double getPrecision() {
        double tpPlusfp = truePositives + falsePositives;
        if (tpPlusfp > 0)
            return (100.0 * truePositives) / tpPlusfp;
        return 0;
    }

    @Override
    public String toString() {
        return "Treshold = " + threshold + " FP = " + falsePositives + " TP = " + truePositives + " FN = "
                + falseNegatives + " TN = " + trueNegatives + " Recall = " + getRecall() + " FPR = "
                + getFalsePositiveRate() + " Precision = " + getPrecision();
    }

}
