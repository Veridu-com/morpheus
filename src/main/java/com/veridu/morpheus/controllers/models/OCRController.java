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
 * Created by cassio on 5/23/17.
 */
@RestController
public class OCRController {

    private BeanUtils utils;
    private BeanConfigurationManager beanManager;
    private ITask ocrTask;

    /**
     * Constructor
     * @param utils injected utils
     * @param beanManager injected manager
     * @param ocrTask injected task
     */
    @Autowired
    public OCRController(BeanUtils utils, BeanConfigurationManager beanManager, @Qualifier("ocr") ITask ocrTask) {
        this.utils = utils;
        this.beanManager = beanManager;
        this.ocrTask = ocrTask;
    }

    /**
     * Handle post request
     * @param params request parameters
     * @return model response as json
     */
    @PostMapping("/morpheus/ocr")
    public ModelResponse makePrediction(@RequestBody OCRParameters params) {

        if (!LocalUtils.validateRequestParams(params))
            return new ModelResponse(false);

        this.ocrTask.runTask(params);

        return new ModelResponse(true);
    }

}
