package com.veridu.morpheus.interfaces.models;

public interface IPrediction {

    /**
     * Gets the probability a user is real. When binary classifiers are used they can just set this to 1/0.
     * 
     * @return
     */
    public double realUserProbability();

    /**
     * Gets the probability a user is fake. When binary classifiers are used they can just set this to 1/0.
     * 
     * @return
     */
    public double fakeUserProbability();

    /**
     * Make a prediction based on a minimum threshold. For a user to be considered real (true) he/she has to have a real
     * user probability >= threshold. If he/she fails, false is returned to indicate a fake user.
     * 
     * @param threshold
     * @return
     */
    public boolean thresholdedPrediction(double threshold);

}
