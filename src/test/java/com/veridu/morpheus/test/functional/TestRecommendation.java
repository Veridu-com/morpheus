//package com.veridu.morpheus.test.functional;
//
//import com.veridu.idos.exceptions.SDKException;
//import com.veridu.morpheus.tasks.recommendations.RecommendationTask;
//import org.json.JSONObject;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.io.UnsupportedEncodingException;
//
///**
// * Created by cassio on 12/13/16.
// */
//
//public class TestRecommendation extends MainFunctionalTestSetup {
//
//    @Autowired
//    private RecommendationTask task;
//
//    @Test
//    public void testGateRules() {
//        try {
//            // create some dummy rules for the user
//            factory.getGate().upsert(userName, "testGate1", true, "high");
//            factory.getGate().upsert(userName, "testGate2", false, "low");
//            factory.getGate().upsert(userName, "testGate3", true, "medium");
//
//            // read request:
//            JSONObject req = loadJSONResponse("/recommendations/test-gate.json");
//            task.runTask(req);
//
//
//
//        } catch (SDKException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
