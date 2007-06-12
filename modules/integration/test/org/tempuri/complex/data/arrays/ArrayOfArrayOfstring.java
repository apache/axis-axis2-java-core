package org.tempuri.complex.data.arrays;

public class ArrayOfArrayOfstring {

    protected ArrayOfstring[] arrayOfstring;

    public ArrayOfstring[] getArrayOfstring() {
        if (arrayOfstring == null) {
            arrayOfstring = new ArrayOfstring[0];
        }
        return this.arrayOfstring;
    }


    public void setArrayOfstring(ArrayOfstring[] arrayOfstring) {
        this.arrayOfstring = arrayOfstring;
    }
}
