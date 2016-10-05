package com.veridu.morpheus.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service
@Scope("singleton")
public class BeanConfigurationManager {

    private JsonObject json;
    private static final Logger log = LoggerFactory.getLogger(BeanConfigurationManager.class);

    @PostConstruct
    private void init() {
        readConfigFile();
        log.info("Configuration Manager bean for Morpheus initialized");
    }

    private void readConfigFile() {
        InputStream is = this.getClass().getResourceAsStream("/config/config.json");
        JsonParser parser = new JsonParser();
        this.json = parser.parse(new InputStreamReader(is)).getAsJsonObject();
    }

    public String getStringProperty(String propertyName) {
        return json.get(propertyName).getAsString();
    }

    public int getIntProperty(String propertyName) {
        return json.get(propertyName).getAsInt();
    }

    public String getObjectStringProperty(String objectName, String propertyName) {
        return json.get(objectName).getAsJsonObject().get(propertyName).getAsString();
    }

    public int getObjectIntProperty(String objectName, String propertyName) {
        return json.get(objectName).getAsJsonObject().get(propertyName).getAsInt();
    }

    public JsonArray getObjectArrayProperty(String objectName, String propertyName) {
        return json.get(objectName).getAsJsonObject().get(propertyName).getAsJsonArray();
    }

    public String getHandlerPrivateKey() {
        return getStringProperty("handlerPrivateKey");
    }

    public String getHandlerPublicKey() {
        return getStringProperty("handlerPublicKey");
    }

}
