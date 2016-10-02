package com.veridu.morpheus.impl;

import java.io.FileReader;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class Utils {

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
