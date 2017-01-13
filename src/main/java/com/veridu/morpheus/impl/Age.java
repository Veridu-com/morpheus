package com.veridu.morpheus.impl;

import com.google.gson.JsonObject;
import com.veridu.idos.IdOSAPIFactory;
import com.veridu.idos.exceptions.SDKException;
import com.veridu.morpheus.utils.LocalUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by cassio on 1/13/17.
 */
public class Age {

    int birthDay;
    int birthMonth;
    int birthYear;

    public Age(int birthDay, int birthMonth, int birthYear) {
        this.birthDay = birthDay;
        this.birthMonth = birthMonth;
        this.birthYear = birthYear;
    }

    public static Age obtainAge(IdOSAPIFactory factory, String userName) throws SDKException {
        JsonObject response;

        int bday, bmonth, byear;

        response = factory.getAttribute().getOne(userName, "birthDay");
        bday = parseIntResponse(response);

        response = factory.getAttribute().getOne(userName, "birthMonth");
        bmonth = parseIntResponse(response);

        response = factory.getAttribute().getOne(userName, "birthYear");
        byear = parseIntResponse(response);

        return new Age(bday, bmonth, byear);
    }

    private static int parseIntResponse(JsonObject response) {
        if (LocalUtils.okResponse(response))
            return Integer.parseInt(response.get("data").getAsJsonObject().get("value").getAsString());
        return -1;
    }

    /**
     * Tells whether the user has a valid age
     *
     * @return
     */
    public boolean validate() {
        return this.birthDay > -1 && this.birthMonth > -1 && this.birthYear > -1;
    }

    /**
     * Compute the user age in years.
     *
     * @return the age in years
     */
    public int obtainAge() {
        Date d = new Date();

        Calendar today = Calendar.getInstance();
        today.setTime(d);

        int currYear = today.get(Calendar.YEAR);
        int currMonth = today.get(Calendar.MONTH);
        int currDay = today.get(Calendar.DAY_OF_MONTH);

        int age = currYear - this.birthYear;

        // check if the user has not had his birthday this year yet
        // if yes, then we need to subtract 1

        if (this.birthMonth > currMonth || (this.birthMonth == currMonth && this.birthDay > currDay))
            age--;

        return age;
    }

}
