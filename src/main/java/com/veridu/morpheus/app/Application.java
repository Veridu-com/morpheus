package com.veridu.morpheus.app;

import com.veridu.morpheus.impl.Constants;
import com.veridu.morpheus.interfaces.models.IModel;
import com.veridu.morpheus.utils.BeanConfigurationManager;
import com.veridu.morpheus.utils.BeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by cassio on 10/2/16.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.veridu.morpheus")
@EnableAsync
public class Application extends AsyncConfigurerSupport {

    public static void main(String[] args) {
        BeanConfigurationManager bm = new BeanConfigurationManager();
        BeanUtils utils = new BeanUtils(bm);
        IModel model = utils.readModel("/models/" + Constants.COUNTRY_MLP_MODEL_NAME);

        SpringApplication.run(Application.class, args);
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(10000);
        executor.setThreadNamePrefix("Task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(10);
        executor.initialize();
        return executor;
    }

}
