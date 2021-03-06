/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

/**
 * Class indicating a name mismatch found by comparing a user's declared name and how
 * he was referred to (tagged) in comments.
 */
public class NameMismatch {

    private String declaredName;
    private String citedAsName;
    private String date;
    private String msgId;

    /**
     * Constructor
     *
     * @param declaredName users declared name
     * @param citedAsName users cited name in a comment
     * @param date date of the message
     * @param msgId message id
     */
    public NameMismatch(String declaredName, String citedAsName, String date, String msgId) {
        this.declaredName = declaredName;
        this.citedAsName = citedAsName;
        this.date = date;
        this.msgId = msgId;
    }

    public String getDeclaredName() {
        return declaredName;
    }

    public void setDeclaredName(String declaredName) {
        this.declaredName = declaredName;
    }

    public String getCitedAsName() {
        return citedAsName;
    }

    public void setCitedAsName(String citedAsName) {
        this.citedAsName = citedAsName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

}
