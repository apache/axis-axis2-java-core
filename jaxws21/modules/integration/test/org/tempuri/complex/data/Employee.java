package org.tempuri.complex.data;

import org.tempuri.complex.data.arrays.ArrayOfshort;

import java.util.Calendar;


public class Employee {

    protected Person baseDetails;
    protected Calendar hireDate;
    protected Long jobID;
    protected ArrayOfshort numbers;

    /**
     * Gets the value of the baseDetails property.
     */
    public Person getBaseDetails() {
        return baseDetails;
    }

    /**
     * Sets the value of the baseDetails property.
     */
    public void setBaseDetails(Person value) {
        this.baseDetails = ((Person) value);
    }

    /**
     * Gets the value of the hireDate property.
     *
     * @return possible object is
     *         {@link Calendar }
     */
    public Calendar getHireDate() {
        return hireDate;
    }

    /**
     * Sets the value of the hireDate property.
     *
     * @param value allowed object is
     *              {@link Calendar }
     */
    public void setHireDate(Calendar value) {
        this.hireDate = value;
    }

    /**
     * Gets the value of the jobID property.
     *
     * @return possible object is
     *         {@link Long }
     */
    public Long getJobID() {
        return jobID;
    }

    /**
     * Sets the value of the jobID property.
     *
     * @param value allowed object is
     *              {@link Long }
     */
    public void setJobID(Long value) {
        this.jobID = value;
    }

    /**
     * Gets the value of the numbers property.
     */
    public ArrayOfshort getNumbers() {
        return numbers;
    }

    /**
     * Sets the value of the numbers property.
     */
    public void setNumbers(ArrayOfshort value) {
        this.numbers = value;
    }

}
