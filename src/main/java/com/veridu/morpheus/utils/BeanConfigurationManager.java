package com.veridu.morpheus.utils;

import org.springframework.stereotype.Service;

//@Startup
//@Singleton
//@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
//@Lock(LockType.READ)
@Service
public class BeanConfigurationManager {

    //    private JsonObject json;
    //    private static final Logger log = Logger.getLogger(BeanConfigurationManager.class);
    //
    //    @PostConstruct
    //    private void init() {
    //        readConfigFile();
    //        log.info("Configuration Manager bean for Skynet initialized");
    //    }
    //
    //    private void readConfigFile() {
    //        InputStream is = this.getClass().getResourceAsStream("/config/config.json");
    //        JsonReader jr = Json.createReader(is);
    //        this.json = jr.readObject();
    //    }
    //
    //    public String getStringProperty(String propertyName) {
    //        return json.getString(propertyName);
    //    }
    //
    //    public int getIntProperty(String propertyName) {
    //        return json.getInt(propertyName);
    //    }
    //
    //    public String getObjectStringProperty(String objectName, String propertyName) {
    //        return json.getJsonObject(objectName).getString(propertyName);
    //    }
    //
    //    public int getObjectIntProperty(String objectName, String propertyName) {
    //        return json.getJsonObject(objectName).getInt(propertyName);
    //    }
    //
    //    public JsonArray getObjectArrayProperty(String objectName, String propertyName) {
    //        return json.getJsonObject(objectName).getJsonArray(propertyName);
    //    }
    //
    //    public String getHandlerPrivateKey() {
    //        return json.getString("handlerPrivateKey");
    //    }
    //
    //    public String getHandlerPublicKey() {
    //        return json.getString("handlerPublicKey");
    //    }

}
