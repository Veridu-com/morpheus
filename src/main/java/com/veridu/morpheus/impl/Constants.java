/*
 * Copyright (c) 2012-2017 Veridu Ltd <https://veridu.com>
 * All rights reserved.
 */

package com.veridu.morpheus.impl;

import java.io.File;

/**
 * Class defining several constants used throught the project, such as training file names,
 * model file names, flags, provider names, etc.
 */
public class Constants {

    public static final boolean DEBUG = false;

    // generic file system constants:

    public static final String WORK_DIR = System.getProperty("user.dir");

    public static final String FILE_SEP = File.separator;

    // training and model generation constants:

    public static final String SKYNET_PROVIDER = "skynet";

    // training file paths;
    public static final String EMAIL_TRAIN_PATH = WORK_DIR + FILE_SEP + "email-train.arff";
    public static final String COUNTRY_TRAIN_PATH = WORK_DIR + FILE_SEP + "country-train.arff";
    public static final String BIRTH_MONTH_TRAIN_PATH = WORK_DIR + FILE_SEP + "birth-month-train.arff";
    public static final String BIRTH_YEAR_MLP_TRAIN_PATH = WORK_DIR + FILE_SEP + "birth-year-mlp-train.arff";
    public static final String BIRTH_DAY_MLP_TRAIN_PATH = WORK_DIR + FILE_SEP + "birth-day-mlp-train.arff";
    public static final String FIRST_NAME_MLP_TRAIN_PATH = WORK_DIR + FILE_SEP + "first-name-mlp-train.arff";
    public static final String LAST_NAME_MLP_TRAIN_PATH = WORK_DIR + FILE_SEP + "last-name-mlp-train.arff";
    public static final String OVERALL_MLP_TRAIN_PATH = WORK_DIR + FILE_SEP + "overall-mlp-train.arff";
    public static final String CITY_TRAIN_PATH = WORK_DIR + FILE_SEP + "city-train.arff";
    public static final String ZIPCODE_TRAIN_PATH = WORK_DIR + FILE_SEP + "zipcode-train.arff";
    public static final String PHONE_TRAIN_PATH = WORK_DIR + FILE_SEP + "phone-train.arff";
    public static final String STREET_TRAIN_PATH = WORK_DIR + FILE_SEP + "street-train.arff";
    public static final String GENDER_TRAIN_PATH = WORK_DIR + FILE_SEP + "gender-train.arff";

    // model paths:
    public static final String MODEL_SUFFIX = ".model";

    public static final String EMAIL_MLP_MODEL_NAME = "email-mlp" + MODEL_SUFFIX;

    public static final String COUNTRY_MLP_MODEL_NAME = "country-mlp" + MODEL_SUFFIX;

    public static final String BIRTH_YEAR_MLP_MODEL_NAME = "birth-year-mlp" + MODEL_SUFFIX;

    public static final String BIRTH_DAY_MLP_MODEL_NAME = "birth-day-mlp" + MODEL_SUFFIX;

    public static final String BIRTH_MONTH_MLP_MODEL_NAME = "birth-month-mlp" + MODEL_SUFFIX;

    public static final String BIRTH_MONTH_RF_MODEL_NAME = "birth-month-rf" + MODEL_SUFFIX;

    public static final String FIRST_NAME_MLP_MODEL_NAME = "first-name-mlp" + MODEL_SUFFIX;

    public static final String LAST_NAME_MLP_MODEL_NAME = "last-name-mlp" + MODEL_SUFFIX;

    public static final String OVERALL_MLP_MODEL_NAME = "overall-mlp" + MODEL_SUFFIX;

    public static final String OVERALL_COST_SENSITIVE_NB_LOW_MODEL_NAME = "overall-csnb-low" + MODEL_SUFFIX;
    public static final String OVERALL_COST_SENSITIVE_NB_MED_MODEL_NAME = "overall-csnb-med" + MODEL_SUFFIX;
    public static final String OVERALL_COST_SENSITIVE_NB_HIGH_MODEL_NAME = "overall-csnb-high" + MODEL_SUFFIX;

    public static final String CITY_MLP_MODEL_NAME = "city-mlp" + MODEL_SUFFIX;

    public static final String ZIPCODE_MLP_MODEL_NAME = "zipcode-mlp" + MODEL_SUFFIX;

    public static final String PHONE_MLP_MODEL_NAME = "phone-mlp" + MODEL_SUFFIX;

    public static final String STREET_MLP_MODEL_NAME = "street-mlp" + MODEL_SUFFIX;

    public static final String GENDER_MLP_MODEL_NAME = "gender-mlp" + MODEL_SUFFIX;

    public static final String[] MODEL_NAMES = { EMAIL_MLP_MODEL_NAME, COUNTRY_MLP_MODEL_NAME,
            BIRTH_YEAR_MLP_MODEL_NAME, BIRTH_DAY_MLP_MODEL_NAME, BIRTH_MONTH_MLP_MODEL_NAME, FIRST_NAME_MLP_MODEL_NAME,
            LAST_NAME_MLP_MODEL_NAME, OVERALL_MLP_MODEL_NAME, OVERALL_COST_SENSITIVE_NB_LOW_MODEL_NAME,
            OVERALL_COST_SENSITIVE_NB_MED_MODEL_NAME, OVERALL_COST_SENSITIVE_NB_HIGH_MODEL_NAME, CITY_MLP_MODEL_NAME,
            ZIPCODE_MLP_MODEL_NAME, PHONE_MLP_MODEL_NAME, STREET_MLP_MODEL_NAME, GENDER_MLP_MODEL_NAME };

    // flags

    public static final String FLAG_VALUE = "flag-value";

    public static final String COMPROMISED_FLAG = "compromised-flag";

    public static final String EMPTY_FLAG = "empty-flag";

    public static final String NEW_PROFILE_FLAG = "new-flag";

    public static final String RECENT_CHANGES_FLAG = "recent-changes-flag";

    // prediction response constants:

    public static final String MODEL_NAME_RESPONSE_STR = "ModelName";

    public static final String TIME_TAKEN_RESPONSE_STR = "TimeTaken(ms)";

    public static final String USER_ID_RESPONSE_STR = "UserID";

    public static final String REAL_USR_PROB_RESPONSE_STR = "RealUserProbability";

    // provider names

    public static final String GOOGLE_PROVIDER_NAME = "google";
    public static final String TWITTER_PROVIDER_NAME = "twitter";
    public static final String AMAZON_PROVIDER_NAME = "amazon";
    public static final String FACEBOOK_PROVIDER_NAME = "facebook";
    public static final String CPR_PROVIDER_NAME = "cpr";
    public static final String LINKEDIN_PROVIDER_NAME = "linkedin";
    public static final String PAYPAL_PROVIDER_NAME = "paypal";
    public static final String PERSONAL_PROVIDER_NAME = "personal";
    public static final String SPOTIFY_PROVIDER_NAME = "spotify";
    public static final String YAHOO_PROVIDER_NAME = "yahoo";
    public static final String CHECK_PROVIDER_NAME = "check";
    public static final String DROPBOX_PROVIDER_NAME = "dropbox";
    public static final String SMS_PROVIDER_NAME = "sms";
    public static final String OTP_PROVIDER_NAME = "otp";
    public static final String PROFILE_PROVIDER_NAME = "profile";

    public static final String[] PROVIDERS_NAMES = { GOOGLE_PROVIDER_NAME, TWITTER_PROVIDER_NAME, AMAZON_PROVIDER_NAME,
            FACEBOOK_PROVIDER_NAME, CPR_PROVIDER_NAME, LINKEDIN_PROVIDER_NAME, PAYPAL_PROVIDER_NAME,
            PERSONAL_PROVIDER_NAME, SPOTIFY_PROVIDER_NAME, YAHOO_PROVIDER_NAME, CHECK_PROVIDER_NAME,
            DROPBOX_PROVIDER_NAME };

}
