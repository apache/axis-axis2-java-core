package org.tempuri.complex.data;

public class Person {

    protected Double age;
    protected Float id;
    protected Boolean male;
    protected String name;

    /**
     * Gets the value of the age property.
     *
     * @return possible object is
     *         {@link Double }
     */
    public Double getAge() {
        return age;
    }

    /**
     * Sets the value of the age property.
     *
     * @param value allowed object is
     *              {@link Double }
     */
    public void setAge(Double value) {
        this.age = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     *         {@link Float }
     */
    public Float getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link Float }
     */
    public void setID(Float value) {
        this.id = value;
    }

    /**
     * Gets the value of the male property.
     *
     * @return possible object is
     *         {@link Boolean }
     */
    public Boolean isMale() {
        return male;
    }

    /**
     * Sets the value of the male property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setMale(Boolean value) {
        this.male = value;
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
