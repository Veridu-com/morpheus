/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.utils;

/**
 * Morpheus request parameters
 */
public class Parameters {

    public String publicKey;
    public String userName;
    public int processId = -1;
    public Integer sourceId = -1;
    public boolean verbose = false;

    @Override
    public String toString() {
        return String.format("publicKey: %s userName: %s processId: %d sourceId: %d verbose: %b", publicKey, userName,
                processId, sourceId, verbose);
    }
}
