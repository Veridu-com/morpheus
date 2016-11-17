import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.idos.utils.Filter;
import com.veridu.idos.utils.IdOSAuthType;

import java.util.HashMap;

/**
 * Created by cassio on 11/2/16.
 */
public class CLItest {

    public static void main(String[] args) throws Exception {

        final String userName = "0b3055c547c4a7bf1469";

        HashMap<String, String> credentials = new HashMap<>();
        credentials.put("credentialPublicKey", "e906a642147a4f3b996831f8a9fb7475");
        credentials.put("servicePrivateKey", "213b83392b80ee98c8eb2a9fed9bb84d");
        credentials.put("servicePublicKey", "ef970ffad1f1253a2182a88667233991");
        credentials.put("username", userName);

        IdOSAPIFactory factory = new IdOSAPIFactory(credentials);
        //        factory.getFeature().setAuthType(IdOSAuthType.HANDLER);
        //        System.out.println(factory.getFeature().listAll(userName, Filter.createFilter().addNameFilter("firstName")));
        factory.getFeature().setAuthType(IdOSAuthType.HANDLER);
        JsonObject response = factory.getFeature().listAll(userName, Filter.createFilter().addNameFilter("lastName"));
        System.out.println(response);
    }
}
