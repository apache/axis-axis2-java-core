/**
 * SOAPStruct.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Feb 11, 2005 (10:16:27 LKT) WSDL2Java emitter.
 */

package interop.doclit;

public class SOAPStruct  implements java.io.Serializable {
    private java.lang.String varString;
    private int varInt;
    private float varFloat;

    public SOAPStruct() {
    }

    public SOAPStruct(
           float varFloat,
           int varInt,
           java.lang.String varString) {
           this.varString = varString;
           this.varInt = varInt;
           this.varFloat = varFloat;
    }


    /**
     * Gets the varString value for this SOAPStruct.
     * 
     * @return varString
     */
    public java.lang.String getVarString() {
        return varString;
    }


    /**
     * Sets the varString value for this SOAPStruct.
     * 
     * @param varString
     */
    public void setVarString(java.lang.String varString) {
        this.varString = varString;
    }


    /**
     * Gets the varInt value for this SOAPStruct.
     * 
     * @return varInt
     */
    public int getVarInt() {
        return varInt;
    }


    /**
     * Sets the varInt value for this SOAPStruct.
     * 
     * @param varInt
     */
    public void setVarInt(int varInt) {
        this.varInt = varInt;
    }


    /**
     * Gets the varFloat value for this SOAPStruct.
     * 
     * @return varFloat
     */
    public float getVarFloat() {
        return varFloat;
    }


    /**
     * Sets the varFloat value for this SOAPStruct.
     * 
     * @param varFloat
     */
    public void setVarFloat(float varFloat) {
        this.varFloat = varFloat;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SOAPStruct)) return false;
        SOAPStruct other = (SOAPStruct) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.varString==null && other.getVarString()==null) || 
             (this.varString!=null &&
              this.varString.equals(other.getVarString()))) &&
            this.varInt == other.getVarInt() &&
            this.varFloat == other.getVarFloat();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getVarString() != null) {
            _hashCode += getVarString().hashCode();
        }
        _hashCode += getVarInt();
        _hashCode += new Float(getVarFloat()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

 
}
