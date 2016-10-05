package com.veridu.morpheus.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by cassio on 10/2/16.
 */
@Configuration
@EnableWebSecurity
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {

    private String userName;
    private String password;

    @PostConstruct
    public void init() {
        InputStream fileStream = this.getClass().getResourceAsStream("/application.properties");
        Properties props = new Properties();
        try {
            props.load(fileStream);
            userName = props.getProperty("morpheus.http.user");
            password = props.getProperty("morpheus.http.password");
        } catch (IOException e) {
            e.printStackTrace();
        }

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
