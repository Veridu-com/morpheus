package com.veridu.morpheus.controllers;

import com.veridu.morpheus.impl.ModelResponse;
import com.veridu.morpheus.tasks.BirthdayTask;
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
public class BirthdayController {

    private BeanUtils utils;
    private BeanConfigurationManager beanManager;
    private BirthdayTask birthdayTask;

    @Autowired
    public BirthdayController(BeanUtils utils, BeanConfigurationManager beanManager, BirthdayTask birthdayTask) {
        this.utils = utils;
        this.beanManager = beanManager;
        this.birthdayTask = birthdayTask;
    }

    @PostMapping("/morpheus/birthday")
    public ModelResponse makePrediction(@RequestBody Parameters params) {

        try {
            this.birthdayTask.runTask(params);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new ModelResponse(false);
        }

        return new ModelResponse(true);
    }

}
