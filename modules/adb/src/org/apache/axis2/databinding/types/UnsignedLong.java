/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.math.BigInteger;

/**
 * Custom class for supporting primitive XSD data type UnsignedLong
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#unsignedLong">XML Schema 3.3.21</a>
 */
public class UnsignedLong extends java.lang.Number {

    protected BigInteger lValue = BigInteger.ZERO;
    private static BigInteger MAX = new BigInteger("18446744073709551615"); // max unsigned long

    public UnsignedLong() {
    }

    public UnsignedLong(double value) throws NumberFormatException {
        setValue(new BigInteger(Double.toString(value)));
    }

    public UnsignedLong(BigInteger value) throws NumberFormatException {
        setValue(value);
    }

    public UnsignedLong(long lValue) throws NumberFormatException {
        setValue(BigInteger.valueOf(lValue));
    }

    public UnsignedLong(String stValue) throws NumberFormatException {
        setValue(new BigInteger(stValue));
    }

    private void setValue(BigInteger val) {
        if (!UnsignedLong.isValid(val)) {
            throw new NumberFormatException(
//                    Messages.getMessage("badUnsignedLong00") +
                    String.valueOf(val) + "]");
        }
        this.lValue = val;
    }

    public static boolean isValid(BigInteger value) {
        if (value.compareTo(BigInteger.ZERO) == -1 || // less than zero
                value.compareTo(MAX) == 1) {
            return false;
        }
        return true;
    }

    public String toString() {
        return lValue.toString();
    }

    public int hashCode() {
        if (lValue != null)
            return lValue.hashCode();
        else
            return 0;
    }

    private Object __equalsCalc = null;

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof UnsignedLong)) return false;
        UnsignedLong other = (UnsignedLong) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
                ((lValue == null && other.lValue == null) ||
                (lValue != null &&
                lValue.equals(other.lValue)));
        __equalsCalc = null;
        return _equals;
    }

    // Implement java.lang.Number interface
    public byte byteValue() {
        return lValue.byteValue();
    }

    public short shortValue() {
        return lValue.shortValue();
    }

    public int intValue() {
        return lValue.intValue();
    }

    public long longValue() {
        return lValue.longValue();
    }

    public double doubleValue() {
        return lValue.doubleValue();
    }

    public float floatValue() {
        return lValue.floatValue();
    }

}
