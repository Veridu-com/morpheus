package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.models.IModel;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

import java.util.Random;

public class BasicCV {

    public static void evaluate(IModel model, Instances dataset) {
        try {
            Evaluation eval = new Evaluation(dataset);
            AbstractClassifier classifier = model.getClassifier();
            eval.crossValidateModel(classifier, dataset, 10, new Random(0));
            System.out.println(eval.toSummaryString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
