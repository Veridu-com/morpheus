package com.veridu.morpheus.test.unit;

import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.idos.endpoints.ProfileFeatures;
import com.veridu.idos.exceptions.SDKException;
import com.veridu.idos.utils.Filter;
import com.veridu.morpheus.dataaccess.IdOSAccess;
import com.veridu.morpheus.impl.Fact;
import com.veridu.morpheus.impl.User;
import com.veridu.morpheus.interfaces.facts.IFact;
import com.veridu.morpheus.interfaces.users.IUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by cassio on 11/10/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Filter.class)
public class TestIdOSSQL extends MainTest {

    private static final IUser user = new User("123");
    private static final String provider = "facebook";

    private IdOSAPIFactory factory;
    private IdOSAccess sql;

    @Before
    public void setup() {
        sql = new IdOSAccess();
        factory = mock(IdOSAPIFactory.class);
    }

    @Test
    public void testObtainProviderFactsForUser() {

        JsonObject response = loadJsonResponse("/features-response.json"); // expected response

        try {
            ProfileFeatures profileFeatures = mock(ProfileFeatures.class);

            PowerMockito.mockStatic(Filter.class);
            Filter filter = mock(Filter.class);

            PowerMockito.when(Filter.createFilter()).thenReturn(filter);
            PowerMockito.when(filter.addFeatureSourceNameFilter(provider)).thenReturn(filter);

            when(profileFeatures.listAll(user.getId(), filter)).thenReturn(response);
            when(factory.getFeature()).thenReturn(profileFeatures);

            HashMap<IFact, String> facts = sql.obtainProviderFactsForUser(factory, user, provider);

            IFact expectedBirthYear = new Fact("birthYear", "facebook");
            IFact expectedBirthMonth = new Fact("birthMonth", "facebook");

            assertTrue(facts.containsKey(expectedBirthYear));
            assertTrue(facts.containsKey(expectedBirthMonth));

            assertEquals(1992, Integer.parseInt(facts.get(expectedBirthYear)));
            assertEquals(5, Integer.parseInt(facts.get(expectedBirthMonth)));

        } catch (SDKException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteProviderFactsForUser() {
        JsonObject response = loadJsonResponse("/delete-all-features-response.json"); // expected response

        try {
            ProfileFeatures profileFeatures = mock(ProfileFeatures.class);

            PowerMockito.mockStatic(Filter.class);
            Filter filter = mock(Filter.class);

            PowerMockito.when(Filter.createFilter()).thenReturn(filter);
            PowerMockito.when(filter.addFeatureSourceNameFilter(provider)).thenReturn(filter);

            when(profileFeatures.deleteAll(user.getId(), filter)).thenReturn(response);
            when(factory.getFeature()).thenReturn(profileFeatures);

            int ret = sql.deleteProviderFactsForUser(factory, user, provider);

            assertEquals(1, ret);

        } catch (SDKException e) {
            e.printStackTrace();
        }

    }

    public void testInsertAttributeCandidatesForUser() {

    }

}
