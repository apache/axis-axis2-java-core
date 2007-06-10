package org.tempuri.complex.data.arrays;

public class ArrayOfanyType {

    protected Object[] anyType;

    public Object[] getAnyType() {
        if (anyType == null) {
            anyType = new Object[0];
        }
        return this.anyType;
    }

}
