package com.veridu.morpheus.interfaces.training;

import com.veridu.morpheus.interfaces.users.IFakeUsUser;

import java.util.ArrayList;

/**
 * Created by cassio on 10/6/16.
 */
public interface IFakeUsDataSource {

    public void tearDown();

    public ArrayList<IFakeUsUser> obtainAllFakeUsUsers();

}
