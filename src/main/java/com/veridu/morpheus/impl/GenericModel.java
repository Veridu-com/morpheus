package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.models.IBinaryModel;
import com.veridu.morpheus.interfaces.models.IModel;

import weka.core.Instance;
import weka.core.SerializationHelper;

public abstract class GenericModel implements IBinaryModel {

    private static final long serialVersionUID = -887093987800014116L;

    @Override
    public IModel loadModel(String modelPath) {
        try {
            return (IModel) SerializationHelper.read(modelPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean writeModel(String modelPath) {
        try {
            SerializationHelper.write(modelPath, this);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public double binaryPrediction(Instance instance) throws Exception {
        return -1;
    }

}
