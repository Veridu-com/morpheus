package com.veridu.morpheus.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 * Created by cassio on 10/2/16.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.veridu.morpheus")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
