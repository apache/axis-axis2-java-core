package org.tempuri.complex.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BitMask {

    public static final BitMask BIT_ONE = new BitMask("BitOne");
    public static final BitMask BIT_TWO = new BitMask("BitTwo");
    public static final BitMask BIT_THREE = new BitMask("BitThree");
    public static final BitMask BIT_FOUR = new BitMask("BitFour");
    public static final BitMask BIT_FIVE = new BitMask("BitFive");
    private final String value;
    private static List values = new ArrayList();

    BitMask(String v) {
        value = v;
        values.add(this);
    }

    public String value() {
        return value;
    }

    public static BitMask fromValue(String v) {
        Iterator iterator = values.iterator();
        while (iterator.hasNext()) {
            BitMask c = (BitMask) iterator.next();
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
