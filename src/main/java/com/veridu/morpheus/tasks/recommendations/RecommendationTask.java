package com.veridu.morpheus.tasks.recommendations;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Created by cassio on 12/6/16.
 */
@Component("recommendation")
public class RecommendationTask {

    public void runTask(JSONObject request) {
        System.out.println("---> got a task!!");
    }
}
