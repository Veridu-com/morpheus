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
public class BirthdayCandidateController {

    private ITask birthdayCandidatesTask;

    /**
     * Constructor
     * @param birthdayCandidatesTask injected task
     */
    @Autowired
    public BirthdayCandidateController(@Qualifier("birthday-candidates") ITask birthdayCandidatesTask) {
        this.birthdayCandidatesTask = birthdayCandidatesTask;
    }

    /**
     * Handle Post requests
     *
     * @param params parameter list
     * @return model response
     */
    @PostMapping("/morpheus/birthday-candidates")
    public ModelResponse makePrediction(@RequestBody Parameters params) {

        if (!LocalUtils.validateRequestParams(params))
            return new ModelResponse(false);

        this.birthdayCandidatesTask.runTask(params);

        return new ModelResponse(true);
    }

}
