/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */
package com.veridu.morpheus.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class BeanConfigurationManager {

    private Environment env;

    @Autowired
    public BeanConfigurationManager(Environment env) {
        this.env = env;
    }

    public String getHandlerPrivateKey() {
        return env.getProperty("morpheus.handlerPrivateKey");
    }

    public String getHandlerPublicKey() {
        return env.getProperty("morpheus.handlerPublicKey");
    }

    public String getIDOSAPIURL() {
        if (!env.containsProperty("morpheus.IDOS_DEBUG")
                || Integer.parseInt(env.getProperty("morpheus.IDOS_DEBUG")) == 0)
            return "https://api.idos.io/1.0";
        else
            return env.getProperty("morpheus.IDOS_API_URL");
    }

    public boolean getUseSSLchecking() {
        return Boolean.parseBoolean(env.getProperty("morpheus.useSSL", "true"));
    }

}
