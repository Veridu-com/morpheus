package com.veridu.morpheus.controllers.flags;

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
 * Created by cassio on 4/20/17.
 */
@RestController
public class AutomatedPostingFlagController {

    private ITask autoPostingTask;

    /**
     * Constructor
     * @param autoPostingTask injected task
     */
    @Autowired
    public AutomatedPostingFlagController(@Qualifier("flags-auto") ITask autoPostingTask) {
        this.autoPostingTask = autoPostingTask;
    }

    /**
     * Handle post request
     * @param params request parameters
     * @return model response as json
     */
    @PostMapping("/morpheus/flags-auto")
    public ModelResponse makePrediction(@RequestBody Parameters params) {

        if (!LocalUtils.validateRequestParams(params))
            return new ModelResponse(false);

        this.autoPostingTask.runTask(params);

        return new ModelResponse(true);
    }

}
