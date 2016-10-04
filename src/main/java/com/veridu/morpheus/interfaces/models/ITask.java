package com.veridu.morpheus.interfaces.models;

import com.veridu.morpheus.utils.Parameters;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created by cassio on 10/4/16.
 */
public interface ITask {
    void runTask(@RequestBody Parameters params);
}
