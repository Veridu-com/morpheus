package com.veridu.morpheus.controllers.candidates;

import com.veridu.morpheus.impl.ModelResponse;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.utils.LocalUtils;
import com.veridu.morpheus.utils.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created by cassio on 10/4/16.
 */
public class LastNameCandidateController {

    private ITask lastNameCandidatesTask;

    @Autowired
    public LastNameCandidateController(@Qualifier("lastname-candidates") ITask lastNameCandidatesTask) {
        this.lastNameCandidatesTask = lastNameCandidatesTask;
    }

    @PostMapping("/morpheus/lastname-candidates")
    public ModelResponse makePrediction(@RequestBody Parameters params) {

        if (!LocalUtils.validateRequestParams(params))
            return new ModelResponse(false);

        this.lastNameCandidatesTask.runTask(params);

        return new ModelResponse(true);
    }

}
