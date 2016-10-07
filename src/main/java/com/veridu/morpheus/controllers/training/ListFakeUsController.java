package com.veridu.morpheus.controllers.training;

import com.veridu.morpheus.dataaccess.FakeUsDataSource;
import com.veridu.morpheus.interfaces.users.IFakeUsUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

/**
 * Created by cassio on 10/6/16.
 */
@RestController
public class ListFakeUsController {

    private FakeUsDataSource fakeUsDataSource;

    @Autowired
    public ListFakeUsController(FakeUsDataSource fakeUsDataSource) {
        this.fakeUsDataSource = fakeUsDataSource;
    }

    @GetMapping("/morpheus/fakeus")
    public String getAllFakeUsUsers() {

        String response = "fakeus";
        StringBuffer sb = new StringBuffer();

        ArrayList<IFakeUsUser> fakeUsUsers = this.fakeUsDataSource.obtainAllFakeUsUsers();

        for (IFakeUsUser user : fakeUsUsers) {
            sb.append(user.getId());
            sb.append(",");
        }

        return sb.toString();
    }

}
