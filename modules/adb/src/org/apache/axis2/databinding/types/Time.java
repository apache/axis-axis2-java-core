/*
 * Copyright 2002-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.databinding.types;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/** Class that represents the xsd:time XML Schema type */
public class Time implements java.io.Serializable {

    private static final long serialVersionUID = -9022201555535589908L;

    private Calendar _value;
    private boolean isFromString;
    private String originalString;

    /**
     * a shared java.text.SimpleDateFormat instance used for parsing the basic component of the
     * timestamp
     */
    private static SimpleDateFormat zulu =
            new SimpleDateFormat("HH:mm:ss.SSSSSSSSS'Z'");

    static {
        zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /** Initializes with a Calender. Year, month and date are ignored. */
    public Time(Calendar value) {
        this._value = value;
        _value.set(0, 0, 0);      // ignore year, month, date
    }

    /** Converts a string formatted as HH:mm:ss[.SSS][+/-offset] */
    public Time(String value) throws NumberFormatException {
        _value = makeValue(value);
        this.isFromString = true;
        this.originalString = value;
    }

    /**
     * Returns the time as a calendar. Ignores the year, month and date fields.
     *
     * @return Returns calendar value; may be null.
     */
    public Calendar getAsCalendar() {
        return _value;
    }

    /**
     * Sets the time; ignores year, month, date
     *
     * @param date
     */
    public void setTime(Calendar date) {
        this._value = date;
        _value.set(0, 0, 0);      // ignore year, month, date
    }

    /**
     * Sets the time from a date instance.
     *
     * @param date
     */
    public void setTime(Date date) {
        _value.setTime(date);
        _value.set(0, 0, 0);      // ignore year, month, date
    }

    /** Utility function that parses xsd:time strings and returns a Date object */
    private Calendar makeValue(String source) throws NumberFormatException {

        // cannonical form of the times is  hh ':' mm ':' ss ('.' s+)? (zzzzzz)?

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = null;
        Date date;

        if ((source != null) && (source.length() >= 8)) {
            if (source.length() == 8) {
                // i.e this does not have milisecond values or time zone value
                simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            } else {
                String rest = source.substring(8);
                if (rest.startsWith(".")) {
                    // i.e this have the ('.'s+) part
                    if (rest.endsWith("Z")) {
                        // this is in gmt time zone
                        simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSSSSSSSS'Z'");
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                    } else if ((rest.indexOf("+") > 0) || (rest.indexOf("-") > 0)) {
                        // this is given in a general time zione
                        simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSSSSSSSSz");
                        if (rest.lastIndexOf("+") > 0) {
                            source = source.substring(0, source.lastIndexOf("+")) + "GMT" +
                                    rest.substring(rest.lastIndexOf("+"));
                        } else if (rest.lastIndexOf("-") > 0) {
                            source = source.substring(0, source.lastIndexOf("-")) + "GMT" +
                                    rest.substring(rest.lastIndexOf("-"));
                        }
                    } else {
                        // i.e it does not have time zone
                        simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSSSSSSSS");
                    }

                } else {
                    if (rest.startsWith("Z")) {
                        // this is in gmt time zone
                        simpleDateFormat = new SimpleDateFormat("HH:mm:ss'Z'");
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    } else if (rest.startsWith("+") || rest.startsWith("-")) {
                        // this is given in a general time zione
                        simpleDateFormat = new SimpleDateFormat("HH:mm:ssz");
                        source = source.substring(0, 8) + "GMT" + rest;
                    } else {
                        throw new NumberFormatException("in valid time zone attribute");
                    }
                }
            }
        } else {
            throw new RuntimeException("invalid message string");
        }

        try {
            date = simpleDateFormat.parse(source);
            calendar.setTime(date);
            calendar.set(0, 0, 0);
        } catch (ParseException e) {
            throw new RuntimeException("invalid message string");
        }

        return calendar;
    }


    /**
     * Returns the time as it would be in GMT. This is accurate to the seconds. Milliseconds
     * probably gets lost.
     *
     * @return Returns String.
     */
    public String toString() {
        if (_value == null) {
            return "unassigned Time";
        }

        if (isFromString) {
            return originalString;
        } else {
            synchronized (zulu) {
                return zulu.format(_value.getTime());
            }
        }

    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Time)) return false;
        Time other = (Time)obj;
        if (this == obj) return true;

        boolean _equals;
        _equals = ((_value == null && other._value == null) ||
                (_value != null &&
                        _value.getTime().equals(other._value.getTime())));

        return _equals;

    }

    /**
     * Returns the hashcode of the underlying calendar.
     *
     * @return Returns an <code>int</code> value.
     */
    public int hashCode() {
        return _value == null ? 0 : _value.hashCode();
    }
}
