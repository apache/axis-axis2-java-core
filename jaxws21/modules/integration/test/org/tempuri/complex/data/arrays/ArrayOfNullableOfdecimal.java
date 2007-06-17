package org.tempuri.complex.data.arrays;

import java.math.BigDecimal;

public class ArrayOfNullableOfdecimal {

    protected BigDecimal[] decimal;

    public BigDecimal[] getDecimal() {
        if (decimal == null) {
            decimal = new BigDecimal[0];
        }
        return this.decimal;
    }

    public void setDecimal(BigDecimal[] decimal) {
        this.decimal = decimal;
    }
}
