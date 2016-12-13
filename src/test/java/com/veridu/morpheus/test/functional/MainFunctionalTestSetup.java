//package com.veridu.morpheus.test.functional;
//
//import com.google.gson.JsonObject;
//import com.veridu.idos.IdOSAPIFactory;
//import com.veridu.idos.utils.IdOSAuthType;
//import com.veridu.morpheus.app.Application;
//import com.veridu.morpheus.test.unit.MainTest;
//import org.junit.BeforeClass;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.HashMap;
//
///**
// * Created by cassio on 12/13/16.
// */
//@SpringBootTest(classes = Application.class)
//@RunWith(SpringRunner.class)
//public class MainFunctionalTestSetup extends MainTest {
//
//    protected static IdOSAPIFactory factory;
//    protected static final String userName = "f67b96dcf96b49d713a520ce9f54053c";
//    protected static JsonObject response;
//    protected static JsonObject data;
//
//    @BeforeClass
//    public static void setUpBeforeClass() throws Exception {
//        HashMap<String, String> credentials = new HashMap<>();
//        credentials.put("credentialPublicKey", "4c9184f37cff01bcdc32dc486ec36961");
//        credentials.put("servicePrivateKey", "213b83392b80ee98c8eb2a9fed9bb84d");
//        credentials.put("servicePublicKey", "ef970ffad1f1253a2182a88667233991");
//        credentials.put("username", userName);
//
//        factory = new IdOSAPIFactory(credentials, "http://127.0.0.1:8000/index.php/1.0", true);
//
//        factory.getFeature().setAuthType(IdOSAuthType.HANDLER);
//        factory.getAttribute().setAuthType(IdOSAuthType.HANDLER);
//        factory.getGate().setAuthType(IdOSAuthType.HANDLER);
//        factory.getFlag().setAuthType(IdOSAuthType.HANDLER);
//        factory.getReference().setAuthType(IdOSAuthType.HANDLER);
//        factory.getRecommendation().setAuthType(IdOSAuthType.HANDLER);
//        factory.getScore().setAuthType(IdOSAuthType.HANDLER);
//    }
//
//}
