package org.tempuri.complex.data;

import org.tempuri.complex.data.arrays.ArrayOfPerson;

public class Group {

    protected ArrayOfPerson members;
    protected String name;

    /**
     * Gets the value of the members property.
     */
    public ArrayOfPerson getMembers() {
        return members;
    }

    /**
     * Sets the value of the members property.
     */
    public void setMembers(ArrayOfPerson value) {
        this.members = value;
    }

    /**
     * Gets the value of the name property.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     */
    public void setName(String value) {
        this.name = value;
    }

}
