package com.veridu.morpheus.interfaces.models;

public interface IModelParams {

    /**
     * Gets the string parameters as an array
     * 
     * @return
     */
    public String[] getStringParams();

    /**
     * Gets the integer parameters
     * 
     * @return
     */
    public int[] getIntParams();

    /**
     * Gets the double parameters
     * 
     * @return
     */
    public double[] getDoubleParams();

    /**
     * Gets the boolean parameters
     * 
     * @return
     */
    public boolean[] getBooleanParams();

}
