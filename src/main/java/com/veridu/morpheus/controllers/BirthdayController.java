package com.veridu.morpheus.controllers;

import com.veridu.morpheus.impl.ModelResponse;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by cassio on 10/2/16.
 */
@RestController
public class BirthdayController {

    @PostMapping("/morpheus/birthday")
    public ModelResponse makePrediction() {
        return new ModelResponse("birthday", 1.0);
    }

}
