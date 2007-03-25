
    /**
     * SimpleAddServiceCallbackHandler.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: SNAPSHOT Mar 08, 2007 (04:12:48 LKT)
     */
    package org.apache.axis2.fastinfoset.xsd;

    /**
     *  SimpleAddServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class SimpleAddServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public SimpleAddServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public SimpleAddServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for addStrings method
            * override this method for handling normal response from addStrings operation
            */
           public void receiveResultaddStrings(
                    org.apache.axis2.fastinfoset.xsd.SimpleAddServiceStub.AddStringsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addStrings operation
           */
            public void receiveErroraddStrings(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addFloats method
            * override this method for handling normal response from addFloats operation
            */
           public void receiveResultaddFloats(
                    org.apache.axis2.fastinfoset.xsd.SimpleAddServiceStub.AddFloatsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addFloats operation
           */
            public void receiveErroraddFloats(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addInts method
            * override this method for handling normal response from addInts operation
            */
           public void receiveResultaddInts(
                    org.apache.axis2.fastinfoset.xsd.SimpleAddServiceStub.AddIntsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addInts operation
           */
            public void receiveErroraddInts(java.lang.Exception e) {
            }
                


    }
    