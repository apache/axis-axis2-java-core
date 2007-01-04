
            /**
            * EchoString.java
            *
            * This file was auto-generated from WSDL
            * by the Apache Axis2 version: #axisVersion# #today#
            */

            package server;
            /**
            *  EchoString bean class
            */
        
        public  class EchoString
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://test",
                "echoString",
                "ns1");

            


            /**
            * field for Input
            */

            protected java.lang.String localInput ;
           
           

           /**
           * Auto generated getter method
           * @return java.lang.String
           */
           public  java.lang.String getInput() {
               if (localInput.equals("THROW EXCEPTION"))
                   throw new RuntimeException("test exception");
               return localInput;
           }

           
            
                    /**
                   * Auto generated setter method
                   * @param param Input
                   */
                   public void setInput(java.lang.String param){
                    
                   this.localInput=param;
                   }
                

        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                
                             elementList.add(new javax.xml.namespace.QName("http://test",
                                                                      "input"));
                            
                                    elementList.add(localInput==null?null:
                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localInput));
                                

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }




    

        /**
             *  Factory class that keeps the parse method
             */
           public static class Factory{

               /**
               * static method to create the object
               */
               public static EchoString parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
               EchoString object = new EchoString();
               try {
               int event = reader.getEventType();

              //event better be a START_ELEMENT. if not we should go up to the start element here
               while (event!= javax.xml.stream.XMLStreamReader.START_ELEMENT) {
                   event = reader.next();
               }

               
               if (!MY_QNAME.equals(reader.getName())){
                           throw new Exception("Wrong QName");
               }
               
                       org.apache.axis2.databinding.utils.SimpleElementReaderStateMachine stateMachine1
                         = new org.apache.axis2.databinding.utils.SimpleElementReaderStateMachine();
                       javax.xml.namespace.QName startQname1 = new javax.xml.namespace.QName(
                                            "http://test",
                                           "input");
                       stateMachine1.setElementNameToTest(startQname1);
                       stateMachine1.setNillable();
                       stateMachine1.read(reader);
                       object.setInput(
                         stateMachine1.getText()==null?null:
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                  stateMachine1.getText()));
                             
               } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
               }

               return object;
               }
               }//end of factory class


     

        
        }
           
          