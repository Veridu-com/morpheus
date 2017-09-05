/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.utils;

/**
 * Created by cassio on 5/26/17.
 */
public class PhotoParameters extends Parameters {

    public String docImage; // base64 encoded image
    public String selfieImage; // base64 encoded image

    @Override
    public String toString() {
        return super.toString() + String
                .format(" len(docImage) = %d len(selfie) = %d", docImage.length(), selfieImage.length());
    }

}
