package org.tempuri.complex.data.arrays;

public class ArrayOfstring {

    protected String[] string;

    public String[] getString() {
        if (string == null) {
            string = new String[0];
        }
        return this.string;
    }


    public void setString(String[] string) {
        this.string = string;
    }
}
