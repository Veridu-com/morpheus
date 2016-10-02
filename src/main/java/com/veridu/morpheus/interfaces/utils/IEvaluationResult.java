package com.veridu.morpheus.interfaces.utils;

public interface IEvaluationResult {

    /**
     * Get the false positive rate: FPR = FP / (FP+TN)
     *
     * @return
     */
    public double getFalsePositiveRate();

    /**
     * Get the recall: Recall = TP / (TP + FN)
     *
     * @return
     */
    public double getRecall();

    /**
     * Get the precision: Precision = TP / (TP + FP)
     *
     * @return
     */
    public double getPrecision();

}
