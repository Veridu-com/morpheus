/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
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
@EnableCaching
public class Application extends AsyncConfigurerSupport {

    /**
     * Main application entry point
     *
     * @param args command line args
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Configures the async executor that runs background tasks
     *
     * @return async task executor
     */
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
