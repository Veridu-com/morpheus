package com.veridu.morpheus.dataaccess;

import com.veridu.morpheus.impl.Attribute;
import com.veridu.morpheus.impl.Candidate;
import com.veridu.morpheus.interfaces.facts.IAttribute;
import com.veridu.morpheus.interfaces.facts.ICandidate;
import com.veridu.morpheus.interfaces.training.IFakeUsDataSource;
import com.veridu.morpheus.interfaces.users.IFakeUsUser;
import com.veridu.morpheus.utils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cassio on 10/6/16.
 */
@Component
public class FakeUsDataSource implements IFakeUsDataSource {

    private BeanUtils utils;
    private IdOSAccess idOSAccess;

    private JdbcTemplate jdbcTemplate;

    private DriverManagerDataSource dataSource = null;

    private DriverManagerDataSource getDataSource() {
        if (dataSource == null)
            dataSource = utils.getFakeUsDataSource();
        return dataSource;
    }

    @Autowired
    public FakeUsDataSource(BeanUtils utils, IdOSAccess idOSAccess) {
        this.utils = utils;
        this.idOSAccess = idOSAccess;
        this.jdbcTemplate = new JdbcTemplate(getDataSource());
    }

    @Override
    public void tearDown() {
    }

    @Override
    public ArrayList<IFakeUsUser> obtainAllFakeUsUsers() {

        // FIXME correct this when the Fake Us Portal is in idOS.

        //        List<String> ids = jdbcTemplate.queryForList("SELECT veridu_id FROM verifications", String.class);

        ArrayList<IFakeUsUser> fakeusUsers = new ArrayList<>();

        // the veridu_id maps to the username field on the users table in upstream-api
        ArrayList<String> veriduIds = new ArrayList<>();
        ArrayList<String> verificationIds = new ArrayList<>();

        final String ignoreAddresses = "('cauelorenzato@gmail.com','hakan@veridu.com','flavio@veridu.com','rasgroth@veridu.com', 'cassio@veridu.com', 'alvaro@veridu.com','')";
        final String clientId = "e7f6322e3ab7aea6d5ae3f13b6650c17602c8aef"; // this is the fakeus client id

        final String ignoreVeriduIds =
                "('a738145818ddd6c30a02', '774ec2c7d603747f584f', 'd63eaa917baaf9343eca', 'e8af9b173a35512e4b41', "
                        + "'50909da03fa93a2ef706', 'c2388ff8666e301135e0', '9bc92bc192248509c909', 'cf7af9b68fff55ac0f38', '8d891890bfeabe2aef3e',"
                        + "'dc2a739799009b14c519', '85270c03d32c08e7df9b', '6cf73238890ea575fd71', '0d0edb60f01e7ee88a81', 'cdda5e13277092c937a8')";

        List<Map<String, Object>> rs = this.jdbcTemplate.queryForList(
                "SELECT \"veridu_id\", \"verifications\".\"id\" FROM \"verifications\" JOIN \"users\" ON \"users\".\"id\" = \"verifications\".\"user_id\" AND email NOT IN "
                        + ignoreAddresses + " AND veridu_id NOT IN " + ignoreVeriduIds);

        //        rs.forEach(map -> {
        //            String veriduId = (String) map.get("veridu_id");
        //            int verifId = (Integer) map.get("id");
        //
        //            String userId = dataSource.obtainUserId(veriduId, clientId);
        //            if (userId != null) {
        //                fakeusUsers.add(new FakeUsUser(userId));
        //                veriduIds.add(veriduId);
        //                verificationIds.add(verifId);
        //            }
        //        });

        return new ArrayList<IFakeUsUser>();

        //        Statement st;
        //        try {
        //            st = getConnection().createStatement();
        //            ResultSet rs = st.executeQuery(
        //                    "SELECT \"veridu_id\", \"verifications\".\"id\" FROM \"verifications\" JOIN \"users\" ON \"users\".\"id\" = \"verifications\".\"user_id\" AND email NOT IN "
        //                            + ignoreAddresses + " AND veridu_id NOT IN " + ignoreVeriduIds);
        //            while (rs.next()) {
        //                String veriduId = rs.getString(1);
        //                String verifId = rs.getString(2);
        //                String userId = dataSource.obtainUserId(veriduId, clientId);
        //                if (userId != null) {
        //                    fakeusUsers.add(new FakeUsUser(userId));
        //                    veriduIds.add(veriduId);
        //                    verificationIds.add(verifId);
        //                }
        //
        //            }
        //
        //            rs.close();
        //            st.close();
        //
        //        } catch (SQLException e) {
        //            e.printStackTrace();
        //        }
        //
        //        // add real users from the compiled list
        //        ICsvListReader listReader = null;
        //
        //        try {
        //            // InputStream fileStream = this.getClass().getResourceAsStream("/real-profiles.csv");
        //            InputStream fileStream = this.getClass().getResourceAsStream("/reals.csv");
        //            InputStreamReader insReader = new InputStreamReader(fileStream);
        //
        //            listReader = new CsvListReader(insReader, CsvPreference.STANDARD_PREFERENCE);
        //            final CellProcessor[] processors = new CellProcessor[] { new NotNull() // user id
        //            };
        //
        //            List<Object> idList;
        //
        //            while ((idList = listReader.read(processors)) != null) {
        //                String userId = (String) idList.get(0);
        //                fakeusUsers.add(new FakeUsUser(userId));
        //            }
        //
        //            listReader.close();
        //
        //        } catch (FileNotFoundException e) {
        //            e.printStackTrace();
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        }
        //
        //        // add the profiles to the users
        //        for (IFakeUsUser user : fakeusUsers)
        //            user.setProfiles(dataSource.obtainSingleUserProfiles(user));
        //
        //        // add the attributes with candidates and true/false information
        //        PreparedStatement pst;
        //
        //        // get candidates from the fakeus portal
        //        for (int i = 0; i < verificationIds.size(); i++) {
        //
        //            String verifId = verificationIds.get(i);
        //            IFakeUsUser user = fakeusUsers.get(i);
        //
        //            try {
        //                pst = getConnection().prepareStatement(
        //                        "SELECT \"type\", \"value\", \"result\" FROM \"fields\" WHERE \"verification_id\" = ?");
        //                pst.setInt(1, Integer.parseInt(verifId));
        //
        //                ResultSet rs = pst.executeQuery();
        //
        //                while (rs.next()) {
        //                    String type = rs.getString(1);
        //                    String value = rs.getString(2);
        //                    boolean isReal = rs.getBoolean(3);
        //                    Attribute att = new Attribute(type);
        //                    Candidate cand = new Candidate(value, isReal);
        //                    if (!user.getAttributesMap().containsKey(att))
        //                        user.getAttributesMap().put(att, new ArrayList<ICandidate>());
        //                    // unfortunately we have to check for repeat candidates as well, which
        //                    // is lame, but since there are repeats from the fakeus-db, we have no choice.
        //                    if (!user.getAttributesMap().get(att).contains(cand))
        //                        user.getAttributesMap().get(att).add(cand);
        //                }
        //
        //                rs.close();
        //                pst.close();
        //
        //            } catch (SQLException e) {
        //                e.printStackTrace();
        //            }
        //
        //        }
        //
        //        for (int i = verificationIds.size(); i < fakeusUsers.size(); i++) {
        //            // now fill the candidates of the added users from the compiled list
        //            IFakeUsUser user = fakeusUsers.get(i);
        //            user.setAttributesMap(createMockAttMap());
        //        }
        //
        //        // filter out users that have no attributes - maybe they closed the fakeus portal without completing the
        //        // process? who knows...
        //
        //        Iterator<IFakeUsUser> it = fakeusUsers.iterator();
        //
        //        while (it.hasNext()) {
        //            IFakeUsUser user = it.next();
        //            if (user.getAttributesMap().size() == 0)
        //                it.remove();
        //        }
        //
        //        return fakeusUsers;
    }

    private HashMap<IAttribute, ArrayList<ICandidate>> createMockAttMap() {
        HashMap<IAttribute, ArrayList<ICandidate>> map = new HashMap<>();

        final String[] atts = new String[] { "regionName", "firstName", "middleName", "birthYear", "email", "lastName",
                "gender", "birthDay", "birthMonth", "country", "postalCode", "countryCode", "phone", "streetAddress",
                "city", "overall" };

        for (String att : atts) {
            ArrayList<ICandidate> candidates = new ArrayList<>();
            candidates.add(new Candidate("xxx", true));
            map.put(new Attribute(att), candidates);
        }

        return map;
    }

}
