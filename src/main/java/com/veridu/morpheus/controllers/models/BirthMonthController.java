package com.veridu.morpheus.controllers.models;

import com.veridu.morpheus.impl.ModelResponse;
import com.veridu.morpheus.utils.BeanConfigurationManager;
import com.veridu.morpheus.utils.BeanUtils;
import com.veridu.morpheus.utils.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by cassio on 10/2/16.
 */
@RestController
public class BirthMonthController {

    private BeanUtils utils;
    private BeanConfigurationManager beanManager;
    private BirthMonthTask birthMonthTask;

    @Autowired
    public BirthMonthController(BeanUtils utils, BeanConfigurationManager beanManager, BirthMonthTask birthMonthTask) {
        this.utils = utils;
        this.beanManager = beanManager;
        this.birthMonthTask = birthMonthTask;
    }

    @PostMapping("/morpheus/birthmonth")
    public ModelResponse makePrediction(@RequestBody Parameters params) {

        try {
            this.birthMonthTask.runTask(params);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new ModelResponse(false);
        }

        return new ModelResponse(true);
    }

}
