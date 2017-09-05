/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.controllers.models;

import com.veridu.morpheus.impl.ModelResponse;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by cassio on 5/26/17.
 */
@RestController
public class PhotoController {

    private BeanUtils utils;
    private BeanConfigurationManager beanManager;
    private ITask photoTask;

    /**
     * Constructor
     * @param utils injected utils
     * @param beanManager injected manager
     * @param photoTask injected task
     */
    @Autowired
    public PhotoController(BeanUtils utils, BeanConfigurationManager beanManager, @Qualifier("photo") ITask photoTask) {
        this.utils = utils;
        this.beanManager = beanManager;
        this.photoTask = photoTask;
    }

    /**
     * Handle post request
     * @param params request parameters
     * @return model response as json
     */
    @PostMapping("/morpheus/photo")
    public ModelResponse makePrediction(@RequestBody PhotoParameters params) {

        if (!LocalUtils.validateRequestParams(params))
            return new ModelResponse(false);

        this.photoTask.runTask(params);

        return new ModelResponse(true);
    }
}
