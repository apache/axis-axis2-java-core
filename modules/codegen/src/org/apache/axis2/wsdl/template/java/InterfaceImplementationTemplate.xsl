<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
    <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
    <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>
    <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
    <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
    <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
    <xsl:variable name="dbpackage"><xsl:value-of select="@dbsupportpackage"/></xsl:variable>
    <xsl:variable name="soapVersion"><xsl:value-of select="@soap-version"/></xsl:variable>
    /**
     * <xsl:value-of select="@name"/>.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: #axisVersion# #today#
     */
    package <xsl:value-of select="$package"/>;

        <!-- Put the MTOM enable flag -->


    /*
     *  <xsl:value-of select="@name"/> java implementation 
    */

    public class <xsl:value-of select="@name"/> extends org.apache.axis2.client.Stub implements <xsl:value-of select="$interfaceName"/>{
        //default axis home being null forces the system to pick up the mars from the axis2 library
        public static final String AXIS2_HOME = null;
        protected static org.apache.axis2.description.AxisOperation[] _operations;

        static{

           //creating the Service
           _service = new org.apache.axis2.description.AxisService("<xsl:value-of select="@servicename"/>");

           //creating the operations
           org.apache.axis2.description.AxisOperation __operation;
           _operations = new org.apache.axis2.description.OutInAxisOperation[<xsl:value-of select="count(method)"/>];
      <xsl:for-each select="method">
           __operation = new org.apache.axis2.description.OutInAxisOperation();
           __operation.setName(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>", "<xsl:value-of select="@name"/>"));
           _operations[<xsl:value-of select="position()-1"/>]=__operation;
           _service.addOperation(__operation);
     </xsl:for-each>
       }

       /**
        * Constructor
        */
        public <xsl:value-of select="@name"/>(String axis2Home,String targetEndpoint) throws java.lang.Exception {
		    //creating the configuration
           _configurationContext = new org.apache.axis2.context.ConfigurationContextFactory().buildClientConfigurationContext(axis2Home);
           _configurationContext.getAxisConfiguration().addService(_service);
           _serviceContext =new org.apache.axis2.context.ServiceGroupContext(_configurationContext, _service.getParent()).getServiceContext(_service.getName());
           _clientOptions.setTo(new org.apache.axis2.addressing.EndpointReference(targetEndpoint));

        <!--  Set the soap version depending on the binding. Default is 1.1 so don't set anything for that case-->
        <xsl:if test="$soapVersion='1.2'">
            //Set the soap version
            _clientOptions.setSoapVersionURI(org.apache.axis2.soap.SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        </xsl:if>



        }

        /**
        * Default Constructor
        */
        public <xsl:value-of select="@name"/>() throws java.lang.Exception {
         <!-- change this -->
        <xsl:for-each select="endpoint">
            <xsl:choose>
                <xsl:when test="position()=1">
                     this(AXIS2_HOME,"<xsl:value-of select="."/>" );
                </xsl:when>
                <xsl:otherwise>
                     //this(AXIS2_HOME,"<xsl:value-of select="."/>" );
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="@policyRef">
                //this has a policy ! The policy is written to the follwing file<xsl:value-of select="@policyRef"></xsl:value-of>
            </xsl:if>
        </xsl:for-each>

        }



     <xsl:for-each select="method">
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>
         <xsl:variable name="style"><xsl:value-of select="@style"></xsl:value-of></xsl:variable>
        <xsl:variable name="dbsupportclassname"><xsl:value-of select="@dbsupportname"></xsl:value-of></xsl:variable>
         <xsl:variable name="soapAction"><xsl:value-of select="@soapaction"></xsl:value-of></xsl:variable>
         <xsl:variable name="fullsupporterclassname"><xsl:value-of select="$dbpackage"/>.<xsl:value-of select="$dbsupportclassname"/></xsl:variable>
         <xsl:variable name="mep"><xsl:value-of select="@mep"/></xsl:variable>

         <!-- Code generation for the in-out mep -->
		 <xsl:if test="$mep='http://www.w3.org/2004/08/wsdl/in-out'">
         <xsl:if test="$isSync='1'">
        /**
         * Auto generated method signature
         * @see <xsl:value-of select="$package"/>.<xsl:value-of select="$interfaceName"/>#<xsl:value-of select="@name"/>
         <xsl:for-each select="input/param[@type!='']">
         * @param <xsl:value-of select="@name"></xsl:value-of><xsl:text>
         </xsl:text></xsl:for-each>
         */
        public <xsl:choose><xsl:when test="$outputtype=''">void</xsl:when><xsl:otherwise><xsl:value-of select="$outputtype"/></xsl:otherwise></xsl:choose>
        <xsl:text> </xsl:text><xsl:value-of select="@name"/>(
         <xsl:for-each select="input/param[@type!='']">
            <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
         </xsl:for-each>) throws java.rmi.RemoteException{

		     org.apache.axis2.client.Call _call = new org.apache.axis2.client.Call(_serviceContext);
             org.apache.axis2.client.Options _options = new org.apache.axis2.client.Options(_clientOptions);
             _call.setClientOptions(_options);

 		     org.apache.axis2.context.MessageContext _messageContext = getMessageContext();
            _options.setSoapAction("<xsl:value-of select="$soapAction"/>");
             <xsl:for-each select="input/param[@Action!='']">_options.setAction("<xsl:value-of select="@Action"/>");</xsl:for-each>

             //set the properties
            populateModules(_call);

            org.apache.axis2.soap.SOAPEnvelope env = null;
            env = createEnvelope();
            <xsl:variable name="count"><xsl:value-of select="count(input/param[@type!=''])"></xsl:value-of></xsl:variable>
            <xsl:choose>
            <!-- test the number of input parameters
				  If the number of parameter is more then just run the normal test-->
                <xsl:when test="$count>0">
                  <xsl:choose>
                      <xsl:when test="$style='rpc'">
               // Style is RPC
              org.apache.axis2.rpc.client.RPCStub.setValueRPC(getFactory(_options.getSoapVersionURI(), env,"<xsl:value-of select="@namespace"/>","<xsl:value-of select="@name"/>",
              new String[]{<xsl:for-each select="input/param[@type!='']"><xsl:if test="position()>1">,</xsl:if>"<xsl:value-of select="@name"/>"</xsl:for-each>},
              new Object[]{<xsl:for-each select="input/param[@type!='']"><xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@name"/></xsl:for-each>});
                      </xsl:when>
                      <xsl:when test="$style='doc'">

               //Style is Doc.
                <xsl:for-each select="input/param[@location='body']">
               setValueDoc(env,<xsl:value-of select="$fullsupporterclassname"/>.toOM(<xsl:value-of select="@name"/>));
                 </xsl:for-each>
                 <xsl:for-each select="input/param[@location='header']">
               setValueDoc(env,<xsl:value-of select="$fullsupporterclassname"/>.toOM(<xsl:value-of select="@name"/>),true);
                 </xsl:for-each>
                 </xsl:when>
                 <xsl:otherwise>
               //Unknown style!! No code is generated
               throw java.lang.UnsupportedOperationException("Unknown Style");
                      </xsl:otherwise>
                  </xsl:choose>
              </xsl:when>
              <!-- No input parameters present. So generate assuming no input parameters-->
              <xsl:otherwise>
                   <xsl:choose>
                   <xsl:when test="$style='rpc'">
               //Style is RPC. No input parameters
               org.apache.axis2.rpc.client.RPCStub.setValueRPC(getFactory(_options.getSoapVersionURI()), env,"<xsl:value-of select="@namespace"/>","<xsl:value-of select="@name"/>",null,null);
                      </xsl:when>
                     <xsl:when test="$style='doc'">
               //Style is Doc. No input parameters
               setValueDoc(env,null);
                      </xsl:when>
                      <xsl:otherwise>
             //Unknown style!! No code is generated
              throw UnsupportedOperationException("Unknown Style");
                      </xsl:otherwise>
                  </xsl:choose>
             </xsl:otherwise>
            </xsl:choose>

             _messageContext.setEnvelope(env);
             <xsl:choose>
             <xsl:when test="$outputtype=''">
             _call.invokeBlocking(_operations[<xsl:value-of select="position()-1"/>], _messageContext);
               return;
              </xsl:when>
              <xsl:otherwise>
             //set the exception throwing status
             _call.getClientOptions().setExceptionToBeThrownOnSOAPFault(true);
             org.apache.axis2.context.MessageContext  _returnMessageContext = _call.invokeBlocking(_operations[<xsl:value-of select="position()-1"/>], _messageContext);
             org.apache.axis2.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
             java.lang.Object object = <xsl:value-of select="$fullsupporterclassname"/>.fromOM(getElement(_returnEnv,"<xsl:value-of select="$style"/>"),<xsl:value-of select="$outputtype"/>.class);
             return (<xsl:value-of select="$outputtype"/>)object;
                 </xsl:otherwise>
             </xsl:choose>

            <!-- this needs to be changed -->
        }
        </xsl:if>
        <xsl:if test="$isAsync='1'">
         /**
         * Auto generated method signature for Asynchronous Invocations
         * @see <xsl:value-of select="$package"/>.<xsl:value-of select="$interfaceName"/>#start<xsl:value-of select="@name"/>
          <xsl:for-each select="input/param[@type!='']">
         * @param <xsl:value-of select="@name"></xsl:value-of><xsl:text>
         </xsl:text></xsl:for-each>
         */
        public  void start<xsl:value-of select="@name"/>(
         <xsl:variable name="paramCount"><xsl:value-of select="count(input/param[@type!=''])"></xsl:value-of></xsl:variable>
         <xsl:for-each select="input/param[@type!='']">
            <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"></xsl:value-of></xsl:for-each>
            <xsl:if test="$paramCount>0">,</xsl:if>final <xsl:value-of select="$package"/>.<xsl:value-of select="$callbackname"/> callback) throws java.rmi.RemoteException{

             org.apache.axis2.client.Call _call = new org.apache.axis2.client.Call(_serviceContext);
            org.apache.axis2.client.Options _options = new org.apache.axis2.client.Options(_clientOptions);
             _call.setClientOptions(_options);
 		     org.apache.axis2.context.MessageContext _messageContext = getMessageContext();
             _options.setSoapAction("<xsl:value-of select="$soapAction"/>");
            
            <xsl:for-each select="input/param[@Action!='']">_options.setAction("<xsl:value-of select="@Action"/>");</xsl:for-each>

             org.apache.axis2.soap.SOAPEnvelope env = createEnvelope();
             <xsl:choose>
             <!-- There are more than 1 parameter in the input-->
              <xsl:when test="$paramCount>0">
              <xsl:choose>
               <xsl:when test="$style='rpc'">
           // Style is RPC
           org.apache.axis2.rpc.client.RPCStub.setValueRPC(getFactory(_options.getSoapVersionURI()), env,
            "<xsl:value-of select="@namespace"/>",
            "<xsl:value-of select="@name"/>",
             new String[]{<xsl:for-each select="input/param[@type!='']"><xsl:if test="position()>1">,</xsl:if>"<xsl:value-of select="@name"/>"</xsl:for-each>},
             new Object[]{<xsl:for-each select="input/param[@type!='']"><xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@name"/></xsl:for-each>});
                      </xsl:when>

                      <xsl:when test="$style='doc'">
           //Style is Doc
            setValueDoc(env,<xsl:value-of select="$fullsupporterclassname"/>.toOM(<xsl:value-of select="input/param[1]/@name"/>));
                      </xsl:when>
                      <xsl:otherwise>
          //Unknown style!! No code is generated
          throw UnsupportedOperationException("Unknown Style");
                      </xsl:otherwise>
                  </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                   <xsl:choose>
                   <xsl:when test="$style='rpc'">
           //Style is RPC. No input parameters
              org.apache.axis2.rpc.client.RPCStub.setValueRPC(getFactory(_options.getSoapVersionURI()), env,
                "<xsl:value-of select="@namespace"/>",
                "<xsl:value-of select="@name"/>",
                null,
                null);
                      </xsl:when>
                      <!-- The follwing code is specific to XML beans-->
                      <xsl:when test="$style='doc'">
           //Style is Doc. No input parameters
           setValueDoc(env,null);
                      </xsl:when>
                      <xsl:otherwise>
          //Unknown style!! No code is generated
          throw UnsupportedOperationException("Unknown Style");
                      </xsl:otherwise>
                  </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>
             _messageContext.setEnvelope(env);
             <xsl:choose>
             <xsl:when test="$outputtype=''">
              //Nothing to pass as the callback!!!
              _call.invokeNonBlocking(_operations[<xsl:value-of select="position()-1"/>], _messageContext,null);
              </xsl:when>
              <xsl:otherwise>
               _call.invokeNonBlocking(_operations[<xsl:value-of select="position()-1"/>], _messageContext, new org.apache.axis2.client.async.Callback(){
                public void onComplete(org.apache.axis2.client.async.AsyncResult result){

			    java.lang.Object object = <xsl:value-of select="$fullsupporterclassname"/>.fromOM(getElement(result.getResponseEnvelope(),"<xsl:value-of select="$style"/>"),<xsl:value-of select="$outputtype"/>.class);
                             callback.receiveResult<xsl:value-of select="@name"/>((<xsl:value-of select="$outputtype"/>)object);
                }
                public void reportError(java.lang.Exception e){
                      callback.receiveError<xsl:value-of select="@name"/>(e);
                }
             }
            );
              </xsl:otherwise>
             </xsl:choose>
        }
      </xsl:if>
      <!-- End of in-out mep -->
      </xsl:if>
      <!-- Start of in only mep-->
      <xsl:if test="$mep='http://www.w3.org/2004/08/wsdl/in-only'">
      <!-- for the in only mep there is no notion of sync or async. And there is no return type also -->
      public void <xsl:text> </xsl:text><xsl:value-of select="@name"/>(
         <xsl:for-each select="input/param[@type!='']">
            <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
         </xsl:for-each>) throws java.rmi.RemoteException{
         org.apache.axis2.client.MessageSender _msgSender = new org.apache.axis2.client.MessageSender(_serviceContext);
            
 		    org.apache.axis2.context.MessageContext _messageContext = getMessageContext();
          org.apache.axis2.client.Options _options = new org.apache.axis2.client.Options(_clientOptions);
          _msgSender.setClientOptions(_options);

          _options.setSoapAction("<xsl:value-of select="$soapAction"/>");

          <xsl:for-each select="input/param[@Action!='']">_options.setAction("<xsl:value-of select="@Action"/>");</xsl:for-each>
          org.apache.axis2.soap.SOAPEnvelope env = null;
            env = createEnvelope();
            <xsl:choose>
            <!-- test the number of input parameters
				  If the number of parameter is more then just run the normal generation-->
              <xsl:when test="count(input/param[@type!=''])>0">
                  <xsl:choose>
                      <xsl:when test="$style='rpc'">
               // Style is RPC
              org.apache.axis2.rpc.client.RPCStub.setValueRPC(getFactory(_options.getSoapVersionURI()), env,"<xsl:value-of select="@namespace"/>","<xsl:value-of select="@name"/>",
              new String[]{<xsl:for-each select="input/param[@type!='']"><xsl:if test="position()>1">,</xsl:if>"<xsl:value-of select="@name"/>"</xsl:for-each>},
              new Object[]{<xsl:for-each select="input/param[@type!='']"><xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@name"/></xsl:for-each>});
                      </xsl:when>
                      <xsl:when test="$style='doc'">
                      <!-- for the doc lit case there can be only one element. So take the first element -->
               //Style is Doc.
               setValueDoc(env,<xsl:value-of select="$fullsupporterclassname"/>.toOM(<xsl:value-of select="input/param[1]/@name"/>));
                      </xsl:when>
                      <xsl:otherwise>
               //Unknown style!! No code is generated
               throw java.lang.UnsupportedOperationException("Unknown Style");
                      </xsl:otherwise>
                  </xsl:choose>
              </xsl:when>
              <!-- No input parameters present. So generate assuming no input parameters-->
              <xsl:otherwise>
                   <xsl:choose>
                   <xsl:when test="$style='rpc'">
               //Style is RPC. No input parameters
               org.apache.axis2.rpc.client.RPCStub.setValueRPC(getFactory(_options.getSoapVersionURI()), env,"<xsl:value-of select="@namespace"/>","<xsl:value-of select="@name"/>",null,null);
                      </xsl:when>
                     <xsl:when test="$style='doc'">
               //Style is Doc. No input parameters
               setValueDoc(env,null);
                      </xsl:when>
                      <xsl:otherwise>
             //Unknown style!! No code is generated
              throw UnsupportedOperationException("Unknown Style");
                      </xsl:otherwise>
                  </xsl:choose>
             </xsl:otherwise>
            </xsl:choose>
            
            //set the properties
            populateModules(_msgSender);
            
             _messageContext.setEnvelope(env);
             _msgSender.send(_operations[<xsl:value-of select="position()-1"/>], _messageContext);
               return;
          }
      </xsl:if>
     </xsl:for-each>
    }
    </xsl:template>
 </xsl:stylesheet>
