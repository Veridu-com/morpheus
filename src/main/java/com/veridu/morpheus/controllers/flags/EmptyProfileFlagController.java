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
 * Created by cassio on 10/4/16.
 */
@RestController
public class EmptyProfileFlagController {

    private ITask profileTask;

    @Autowired
    public EmptyProfileFlagController(@Qualifier("flags-empty") ITask profileTask) {
        this.profileTask = profileTask;
    }

    @PostMapping("/morpheus/flags-empty")
    public ModelResponse makePrediction(@RequestBody Parameters params) {

        if (!LocalUtils.validateRequestParams(params))
            return new ModelResponse(false);

        this.profileTask.runTask(params);

        return new ModelResponse(true);
    }

}
