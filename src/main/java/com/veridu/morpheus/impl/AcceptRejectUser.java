package com.veridu.morpheus.impl;

import com.veridu.morpheus.interfaces.users.IAcceptRejectUser;
import com.veridu.morpheus.interfaces.users.IProfile;

import java.util.ArrayList;

public class AcceptRejectUser extends User implements IAcceptRejectUser {

    private boolean isReal;

    public AcceptRejectUser(String id) {
        super(id);
    }

    public AcceptRejectUser(String id, boolean isReal) {
        super(id);
        this.isReal = isReal;
    }

    public AcceptRejectUser(String id, ArrayList<IProfile> profiles, boolean isReal) {
        super(id, profiles);
        this.isReal = isReal;
    }

    @Override
    public boolean isReal() {
        return isReal;
    }

}
