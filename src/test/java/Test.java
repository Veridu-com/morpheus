import com.veridu.idos.IdOSAPIFactory;
import com.veridu.idos.utils.Filter;
import com.veridu.idos.utils.IdOSAuthType;

import java.util.HashMap;

/**
 * Created by cassio on 11/2/16.
 */
public class Test {

    public static void main(String[] args) throws Exception {

        final String userName = "f67b96dcf96b49d713a520ce9f54053c";

        HashMap<String, String> credentials = new HashMap<>();
        credentials.put("credentialPublicKey", "4c9184f37cff01bcdc32dc486ec36961");
        credentials.put("servicePrivateKey", "213b83392b80ee98c8eb2a9fed9bb84d");
        credentials.put("servicePublicKey", "ef970ffad1f1253a2182a88667233991");
        credentials.put("username", userName);

        IdOSAPIFactory factory = new IdOSAPIFactory(credentials);
        factory.getFeature().setAuthType(IdOSAuthType.HANDLER);
        System.out.println(factory.getFeature().listAll(userName, Filter.createFilter().addNameFilter("firstName")));
    }
}
