/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

import java.io.FileReader;
import java.io.IOException;

public class Utils {

    /**
     * Read an ARFF dataset to memory
     *
     * @param path file system path to the file
     * @return loaded instances
     */
    public static Instances readARFFdataset(String path) {
        ArffReader ar = null;
        try {
            ar = new ArffReader(new FileReader(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Instances dataset = ar.getData();
        dataset.setClassIndex(dataset.numAttributes() - 1);
        return dataset;
    }

}
