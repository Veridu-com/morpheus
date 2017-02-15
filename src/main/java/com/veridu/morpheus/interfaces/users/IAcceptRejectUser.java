/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.interfaces.users;

public interface IAcceptRejectUser extends IUser {

    /**
     * indicates whether it is a real user on acceptReject
     *
     * @return
     */
    public boolean isReal();

}
