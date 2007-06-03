package org.tempuri.complex.data;

public class Furniture {

    protected String color;
    protected Float price;

    /**
     * Gets the value of the color property.
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     */
    public void setColor(String value) {
        this.color = ((String) value);
    }

    /**
     * Gets the value of the price property.
     *
     * @return possible object is
     *         {@link Float }
     */
    public Float getPrice() {
        return price;
    }

    /**
     * Sets the value of the price property.
     *
     * @param value allowed object is
     *              {@link Float }
     */
    public void setPrice(Float value) {
        this.price = value;
    }

}
