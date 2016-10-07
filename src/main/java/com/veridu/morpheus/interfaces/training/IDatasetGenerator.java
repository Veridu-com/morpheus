package com.veridu.morpheus.interfaces.training;

import weka.core.Instances;

/**
 * Created by cassio on 10/6/16.
 */
public interface IDatasetGenerator {

    public Instances createDataset();

}
