/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.controllers.recommendations;

import com.veridu.morpheus.impl.ModelResponse;
import com.veridu.morpheus.impl.ModelResponseFailure;
import com.veridu.morpheus.tasks.recommendations.RecommendationTask;
import com.veridu.morpheus.utils.BeanConfigurationManager;
import com.veridu.morpheus.utils.BeanUtils;
import org.apache.log4j.Logger;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.InputStream;

/**
 * Created by cassio on 12/6/16.
 */
@RestController
public class RecommendationController {

    private BeanUtils utils;
    private BeanConfigurationManager beanManager;
    private RecommendationTask recommendationTask;

    private static final Logger logger = Logger.getLogger(RecommendationController.class);

    private Schema schema;

    /**
     * Constructor
     * @param utils injected utils
     * @param beanManager injected manager
     * @param recommendationTask injected task
     */
    @Autowired
    public RecommendationController(BeanUtils utils, BeanConfigurationManager beanManager,
            RecommendationTask recommendationTask) {
        this.utils = utils;
        this.beanManager = beanManager;
        this.recommendationTask = recommendationTask;
    }

    /**
     * Called after the class is instantiated.
     * Reads the recommendation json schema from a file and loads it to memory.
     */
    @PostConstruct
    public void init() {
        // read recommendations schema
        InputStream is = this.getClass().getResourceAsStream("/recommendation-schema.json");
        JSONObject rawSchema = new JSONObject(new JSONTokener(is));
        this.schema = SchemaLoader.load(rawSchema);
    }

    /**
     * Handle post request
     * @param recommendationInput input request encoded as json
     * @return model response as json
     */
    @PostMapping("/morpheus/recommendation")
    public ModelResponse makePrediction(@RequestBody String recommendationInput) {

        // parse json request
        JSONTokener tokener = new JSONTokener(recommendationInput);
        JSONObject request = new JSONObject(tokener);

        // create json from string input
        try {
            this.schema.validate(request);
        } catch (ValidationException e) {
            logger.error("Schema could not be validated.");
            e.printStackTrace();
            return new ModelResponseFailure(false, "Json validation failed for input rules");
        }

        this.recommendationTask.runTask(request);

        return new ModelResponse(true);
    }

}
