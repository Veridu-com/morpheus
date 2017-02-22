/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.annotation.PostConstruct;

/**
 * Configure the Srping web application security.
 * This is mainly so we can use HTTP basic auth,
 * but we can do HTTPS if we want here as well ;-)
 */
@Configuration
@EnableWebSecurity
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {

    private String userName;
    private String password;

    private Environment environment;

    @Autowired
    public ApplicationSecurity(Environment environment) {
        super();
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        userName = environment.getProperty("morpheus.http.user");
        password = environment.getProperty("morpheus.http.password");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser(userName).password(password).roles("USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().ignoringAntMatchers("/**").and().authorizeRequests().antMatchers("/**").hasRole("USER").and()
                .httpBasic();
    }

}
