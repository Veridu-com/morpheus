package com.veridu.morpheus.controllers.models;

import com.veridu.morpheus.impl.ModelResponse;
import com.veridu.morpheus.interfaces.models.ITask;
import com.veridu.morpheus.utils.LocalUtils;
import com.veridu.morpheus.utils.PhotoParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by cassio on 6/7/17.
 */
@RestController
public class PhotoQualityCheckController {

    private ITask photoTask;

    /**
     * Constructor
     * @param photoTask injected task
     */
    @Autowired
    public PhotoQualityCheckController(@Qualifier("photo-qa") ITask photoTask) {
        this.photoTask = photoTask;
    }

    /**
     * Handle post request
     * @param params request parameters
     * @return model response as json
     */
    @PostMapping("/morpheus/photo-qa")
    public ModelResponse makePrediction(@RequestBody PhotoParameters params) {

        if (!LocalUtils.validateRequestParams(params))
            return new ModelResponse(false);

        this.photoTask.runTask(params);

        return new ModelResponse(true);
    }

}
