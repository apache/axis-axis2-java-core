package org.tempuri.complex.data.arrays;

import org.tempuri.complex.data.Person;

public class ArrayOfPerson {

    protected Person[] person;

    public Person[] getPerson() {
        if (person == null) {
            person = new Person[0];
        }
        return this.person;
    }

    public void setPerson(Person[] person){
        this.person =person;
    }

}
