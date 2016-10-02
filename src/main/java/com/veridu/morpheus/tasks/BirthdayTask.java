package com.veridu.morpheus.tasks;

import com.veridu.idos.IdOSAPIFactory;
import com.veridu.idos.exceptions.InvalidToken;
import com.veridu.idos.exceptions.SDKException;
import com.veridu.idos.utils.IdOSAuthType;
import com.veridu.morpheus.utils.BeanUtils;
import com.veridu.morpheus.utils.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Created by cassio on 10/2/16.
 */
@Service
public class BirthdayTask {

    private BeanUtils utils;

    @Autowired
    public BirthdayTask(BeanUtils utils) {
        this.utils = utils;
    }

    @Async
    public void runTask(Parameters params) throws InterruptedException {
        System.out.println("recebi username => " + params.userName);
        System.out.println("recebi pubkey => " + params.publicKey);
        IdOSAPIFactory factory = utils.getIdOSAPIFactory(utils.generateCredentials(params.publicKey, params.userName));
        try {
            factory.getProfile().setAuthType(IdOSAuthType.HANDLER);
            System.out.println(factory.getFeature().listAll(params.userName));
        } catch (InvalidToken invalidToken) {
            invalidToken.printStackTrace();
        } catch (SDKException e) {
            e.printStackTrace();
        }
    }

}
