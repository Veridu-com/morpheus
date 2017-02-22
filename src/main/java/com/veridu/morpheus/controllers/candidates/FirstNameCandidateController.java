/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.controllers.candidates;

import com.veridu.morpheus.impl.ModelResponse;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.utils.LocalUtils;
import com.veridu.morpheus.utils.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by cassio on 10/4/16.
 */
@RestController
public class FirstNameCandidateController {

    private ITask firstNameCandidatesTask;

    /**
     * Constructor
     * @param firstNameCandidatesTask injected task
     */
    @Autowired
    public FirstNameCandidateController(@Qualifier("firstname-candidates") ITask firstNameCandidatesTask) {
        this.firstNameCandidatesTask = firstNameCandidatesTask;
    }

    /**
     * Handle post request
     * @param params request parameters
     * @return model response as json
     */
    @PostMapping("/morpheus/firstname-candidates")
    public ModelResponse makePrediction(@RequestBody Parameters params) {

        if (!LocalUtils.validateRequestParams(params))
            return new ModelResponse(false);

        this.firstNameCandidatesTask.runTask(params);

        return new ModelResponse(true);
    }

}
