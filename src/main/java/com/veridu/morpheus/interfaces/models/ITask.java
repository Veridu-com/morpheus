/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.models;

import com.veridu.morpheus.utils.Parameters;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Interface for an async task
 */
public interface ITask {

    /**
     * Run method that will be executed asynchronously
     * @param params request parameters
     */
    void runTask(@RequestBody Parameters params);
}
