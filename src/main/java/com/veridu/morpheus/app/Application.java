package com.veridu.morpheus.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Created by cassio on 10/2/16.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.veridu.morpheus")
@EnableAsync
public class Application extends AsyncConfigurerSupport {

    public static void main(String[] args) {
        System.out.println("teste");
        SpringApplication.run(Application.class, args);
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("BirthdayTask");
        executor.initialize();
        return executor;
    }

}
