/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.models.IModel;
import com.veridu.morpheus.interfaces.models.IPrediction;

import weka.core.Instance;
import weka.core.Instances;

public class EvaluateModel {

    private static final boolean DEBUG = false;

    /**
     * Evaluates a trained model on a dataset
     *
     * @param model trained model
     * @param dataset evaluation dataset
     * @param threshold value in [0,1] on which to threshold for binary predictions
     * @return evaluation result object
     */
    public EvaluationResult evaluate(IModel model, Instances dataset, double threshold) {
        EvaluationResult evalResult = new EvaluationResult();

        boolean predIsReal;
        double trueClassValue;

        for (Instance inst : dataset) {
            IPrediction predObj;
            try {
                predObj = model.predict(inst);
                predIsReal = predObj.thresholdedPrediction(threshold);
                trueClassValue = inst.classValue();

                if (DEBUG)
                    System.out.println(
                            String.format("instance im analyzing isReal: %s prediction: %s realUserProb: %.2f",
                                    trueClassValue, predIsReal, predObj.realUserProbability()));

                if (predIsReal && (trueClassValue == 1))
                    evalResult.trueNegatives++;
                else if (predIsReal && (trueClassValue == 0))
                    evalResult.falseNegatives++;
                else if (!predIsReal && (trueClassValue == 0))
                    evalResult.truePositives++;
                else
                    evalResult.falsePositives++;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        evalResult.threshold = threshold;

        return evalResult;
    }

}
