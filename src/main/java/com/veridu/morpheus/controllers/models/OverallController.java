package com.veridu.morpheus.controllers.models;

import com.veridu.morpheus.impl.ModelResponse;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.utils.BeanConfigurationManager;
import com.veridu.morpheus.utils.BeanUtils;
import com.veridu.morpheus.utils.LocalUtils;
import com.veridu.morpheus.utils.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by cassio on 10/2/16.
 */
@RestController
public class OverallController {

    private BeanUtils utils;
    private BeanConfigurationManager beanManager;
    private ITask overallTask;

    @Autowired
    public OverallController(BeanUtils utils, BeanConfigurationManager beanManager,
            @Qualifier("overall-mlp") ITask overallTask) {
        this.utils = utils;
        this.beanManager = beanManager;
        this.overallTask = overallTask;
    }

    @PostMapping("/morpheus/overall-mlp")
    public ModelResponse makePrediction(@RequestBody Parameters params) {

        if (!LocalUtils.validateRequestParams(params))
            return new ModelResponse(false);

        this.overallTask.runTask(params);

        return new ModelResponse(true);
    }

}
