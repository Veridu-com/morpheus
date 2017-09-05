/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.utils;

/**
 * Created by cassio on 5/23/17.
 */
public class OCRParameters extends Parameters {

    public String[] names; // names for OCR
    public String image; // base64 encoded image

    @Override
    public String toString() {
        return super.toString() + String
                .format(" names: [%s] len(image) = %d", String.join("; ", names), image.length());
    }
}
