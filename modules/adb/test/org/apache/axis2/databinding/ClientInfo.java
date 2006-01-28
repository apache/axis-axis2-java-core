/**
 * ClientInfo.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */

package org.apache.axis2.databinding;

/**
 * ClientInfo bean class
 */

public class ClientInfo
        implements org.apache.axis2.databinding.ADBBean {
    /* This type was generated from the piece of schema that had
    name = ClientInfo
    Namespace URI = http://www.wso2.com/types
    Namespace Prefix = ns1
    */

    public ClientInfo(String localName, String localSsn) {
        this.localName = localName;
        this.localSsn = localSsn;
    }

    public ClientInfo() {
    }

    /**
     * field for Name
     */
    protected java.lang.String localName;


    /**
     * Auto generated getter method
     *
     * @return java.lang.String
     */
    public java.lang.String getName() {
        return localName;
    }

    /**
     * Auto generated setter method
     *
     * @param param Name
     */
    public void setName(java.lang.String param) {


        this.localName = param;
    }


    /**
     * field for Ssn
     */
    protected java.lang.String localSsn;


    /**
     * Auto generated getter method
     *
     * @return java.lang.String
     */
    public java.lang.String getSsn() {
        return localSsn;
    }

    /**
     * Auto generated setter method
     *
     * @param param Ssn
     */
    public void setSsn(java.lang.String param) {


        this.localSsn = param;
    }


    /**
     * databinding method to get an XML representation of this object
     */
    public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName) {


        java.util.ArrayList elementList = new java.util.ArrayList();
        java.util.ArrayList attribList = new java.util.ArrayList();


        elementList.add(new javax.xml.namespace.QName("",
                "name"));
        elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localName));

        elementList.add(new javax.xml.namespace.QName("",
                "ssn"));
        elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSsn));


        return org.apache.axis2.databinding.utils.ADBPullParser.createPullParser(qName, elementList.toArray(), attribList.toArray());


    }

    /**
     * Factory class that keeps the parse method
     */
    public static class Factory {
        /**
         * static method to create the object
         */
        public static ClientInfo parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
            ClientInfo object = new ClientInfo();
            try {
                int event = reader.getEventType();
                int count = 0;
                int argumentCount = 2;
                boolean done = false;
                //event better be a START_ELEMENT. if not we should go up to the start element here
                while (!reader.isStartElement()) {
                    event = reader.next();
                }


                while (!done) {
                    if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event) {


                        if ("name".equals(reader.getLocalName())) {

                            String content = reader.getElementText();
                            object.setName(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertTostring(content));
                            count++;


                        }


                        if ("ssn".equals(reader.getLocalName())) {

                            String content = reader.getElementText();
                            object.setSsn(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertTostring(content));
                            count++;


                        }


                    }

                    if (argumentCount == count) {
                        done = true;
                    }

                    if (!done) {
                        event = reader.next();
                    }

                }

            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }
    }//end of factory class

}
    