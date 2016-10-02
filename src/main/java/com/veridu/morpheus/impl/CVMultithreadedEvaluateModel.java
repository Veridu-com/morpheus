package com.veridu.morpheus.impl;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.veridu.morpheus.interfaces.models.IModel;

import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

public class CVMultithreadedEvaluateModel {

    private static final int NTHREADS = 8;
    private static final long SEED = 1234;
    private static final int NFOLDS = 10;

    /**
     * Cross validate a model using multiple threads. Hell yes :-)
     * 
     * If you pass a threshold <= 0 then a binary output is used, no thresholds. Pay attention to that.
     * 
     * @param model
     * @param dataset
     * @param threshold
     * @return
     */
    public static EvaluationResult evaluate(IModel model, Instances dataset, double threshold) {

        AbstractClassifier classifier = model.getClassifier();

        final ExecutorService pool = Executors.newFixedThreadPool(NTHREADS);
        final ExecutorCompletionService<CVEvalReturn> completionService = new ExecutorCompletionService<>(pool);

        System.out.println("Randomizing and stratifying dataset...");

        Random rand = new Random(SEED); // create seeded number generator
        Instances randData = new Instances(dataset); // create copy of original data
        randData.randomize(rand); // randomize data with number generator

        randData.stratify(NFOLDS);

        System.out.println("Generating CV folds...");

        ArrayList<Instances> trainFolds = new ArrayList<>();
        ArrayList<Instances> testFolds = new ArrayList<>();

        for (int n = 0; n < NFOLDS; n++) {
            Instances train = randData.trainCV(NFOLDS, n);
            Instances test = randData.testCV(NFOLDS, n);

            trainFolds.add(train);
            testFolds.add(test);
        }

        System.out.println(String.format("Starting cross validation process with %d threads...", NTHREADS));

        for (int i = 0; i < NFOLDS; i++) {
            Callable<CVEvalReturn> worker;
            try {
                if (threshold > 0)
                    worker = new CVThread(AbstractClassifier.makeCopy(classifier), trainFolds.get(i), testFolds.get(i),
                            threshold);
                else
                    worker = new CVThread(AbstractClassifier.makeCopy(classifier), trainFolds.get(i), testFolds.get(i));
                completionService.submit(worker);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        ArrayList<CVEvalReturn> evalReturns = new ArrayList<>();

        for (int i = 0; i < NFOLDS; i++)
            try {
                final Future<CVEvalReturn> future = completionService.take();
                final CVEvalReturn content = future.get();
                evalReturns.add(content);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        pool.shutdown();

        System.out.println("Finished all cross-val threads.");

        System.out.println("Aggregating results...");

        EvaluationResult evalResult = new EvaluationResult();
        evalResult.threshold = threshold;

        for (CVEvalReturn eval : evalReturns)
            for (int i = 0; i < eval.pred.length; i++) {
                boolean predIsReal = eval.pred[i] == 1.0 ? true : false;
                boolean trueClassReal = eval.sup[i] == 1.0 ? true : false;
                if (predIsReal && trueClassReal)
                    evalResult.trueNegatives++;
                else if (predIsReal && !trueClassReal)
                    evalResult.falseNegatives++;
                else if (!predIsReal && !trueClassReal)
                    evalResult.truePositives++;
                else
                    evalResult.falsePositives++;
            }

        System.out.println("********** Recall/FPR analysis **********");
        System.out.println("Results for model: " + model.getClass().getCanonicalName());
        System.out.println(evalResult);

        return evalResult;
    }
}
