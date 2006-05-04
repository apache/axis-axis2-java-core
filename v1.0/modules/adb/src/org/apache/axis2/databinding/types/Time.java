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


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class that represents the xsd:time XML Schema type
 */
public class Time implements java.io.Serializable {
	
    private static final long serialVersionUID = -9022201555535589908L;

	private Calendar _value;

    /**
     * a shared java.text.SimpleDateFormat instance used for parsing the basic
     * component of the timestamp
     */
    private static SimpleDateFormat zulu =
       new SimpleDateFormat("HH:mm:ss.SSS'Z'");

    // We should always format dates in the GMT timezone
    static {
        zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
    }


    /**
     * Initializes with a Calender. Year, month and date are ignored.
     */
    public Time(Calendar value) {
        this._value = value;
        _value.set(0,0,0);      // ignore year, month, date
    }

    /**
     * Converts a string formatted as HH:mm:ss[.SSS][+/-offset]
     */
    public Time(String value) throws NumberFormatException {
        _value = makeValue(value);
    }

    /**
     * Returns the time as a calendar. Ignores the year, month and date fields.
     * @return Returns calendar value; may be null.
     */
    public Calendar getAsCalendar() {
        return _value;
    }

    /**
     * Sets the time; ignores year, month, date
     * @param date
     */
    public void setTime(Calendar date) {
        this._value = date;
        _value.set(0,0,0);      // ignore year, month, date
    }

    /**
     * Sets the time from a date instance.
     * @param date
     */
    public void setTime(Date date) {
        _value.setTime(date);
        _value.set(0,0,0);      // ignore year, month, date
    }

    /**
     * Utility function that parses xsd:time strings and returns a Date object
     */
    private Calendar makeValue(String source) throws NumberFormatException {
        Calendar calendar = Calendar.getInstance();
        Date date;

        validateSource(source);

        // convert what we have validated so far
        date = ParseHoursMinutesSeconds(source);

        int pos = 8;    // The "." in hh:mm:ss.sss

        // parse optional milliseconds
        if ( source != null ) {
            if (pos < source.length() && source.charAt(pos)=='.') {
                int milliseconds = 0;
                int start = ++pos;
                while (pos<source.length() &&
                       Character.isDigit(source.charAt(pos))) {
                    pos++;
                }


                String decimal=source.substring(start,pos);
                if (decimal.length()==3) {
                    milliseconds=Integer.parseInt(decimal);
                } else if (decimal.length() < 3) {
                    milliseconds=Integer.parseInt((decimal+"000")
                                                  .substring(0,3));
                } else {
                    milliseconds=Integer.parseInt(decimal.substring(0,3));
                    if (decimal.charAt(3)>='5') {
                        ++milliseconds;
                    }
                }

                // add milliseconds to the current date
                date.setTime(date.getTime()+milliseconds);
            }

            // parse optional timezone
            if (pos+5 < source.length() &&
                (source.charAt(pos)=='+' || (source.charAt(pos)=='-'))) {
                    if (!Character.isDigit(source.charAt(pos+1)) ||
                        !Character.isDigit(source.charAt(pos+2)) ||
                        source.charAt(pos+3) != ':'              ||
                        !Character.isDigit(source.charAt(pos+4)) ||
                        !Character.isDigit(source.charAt(pos+5)))
                    {
                        throw new NumberFormatException();
                                //Messages.getMessage("badTimezone00"));
                    }

                    int hours = (source.charAt(pos+1)-'0')*10
                        +source.charAt(pos+2)-'0';
                    int mins  = (source.charAt(pos+4)-'0')*10
                        +source.charAt(pos+5)-'0';
                    int milliseconds = (hours*60+mins)*60*1000;

                    // subtract milliseconds from current date to obtain GMT
                    if (source.charAt(pos)=='+') {
                        milliseconds=-milliseconds;
                    }
                    date.setTime(date.getTime()+milliseconds);
                    pos+=6;
            }

            if (pos < source.length() && source.charAt(pos)=='Z') {
                pos++;
                calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            }

            if (pos < source.length()) {
                throw new NumberFormatException();
                        //Messages.getMessage("badChars00"));
            }
        }

        calendar.setTime(date);
        calendar.set(0,0,0);    // ignore year, month, date

        return calendar;
    }

    /**
     * Parses the hours, minutes and seconds of a string, by handing it off to
     * the java runtime.
     * The relevant code returns null if a null string is passed in, so this
     * code may return a null date in response.
     * @param source
     * @return Returns Date.
     * @throws NumberFormatException in the event of trouble
     */
    private static Date ParseHoursMinutesSeconds(String source) {
        Date date;
        try {
            synchronized (zulu) {
                String fulltime = source == null ? null :
                                                    (source.substring(0,8)+".000Z");
                date = zulu.parse(fulltime);
            }
        } catch (Exception e) {
            throw new NumberFormatException(e.toString());
        }
        return date;
    }

    /**
     * Validates the source.
     * @param source
     */
    private void validateSource(String source) {
        // validate fixed portion of format
        if ( source != null ) {
            if (source.charAt(2) != ':' || source.charAt(5) != ':') {
                throw new NumberFormatException();
                        //Messages.getMessage("badTime00"));
            }
            if (source.length() < 8) {
                throw new NumberFormatException();
                        //Messages.getMessage("badTime00"));
            }
        }
    }

    /**
     * Returns the time as it would be in GMT. This is accurate to the
     * seconds. Milliseconds probably gets lost.
     * @return Returns String.
     */
    public String toString() {
        if(_value==null) {
            return "unassigned Time";
        }
        synchronized (zulu) {
            return zulu.format(_value.getTime());
        }

    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Time)) return false;
        Time other = (Time) obj;
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
