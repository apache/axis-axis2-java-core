package org.tempuri.complex.data.arrays;

import java.util.Calendar;

public class ArrayOfNullableOfdateTime {

    protected Calendar[] dateTime;

    public Calendar[] getDateTime() {
        if (dateTime == null) {
            dateTime = new Calendar[0];
        }
        return this.dateTime;
    }

}
