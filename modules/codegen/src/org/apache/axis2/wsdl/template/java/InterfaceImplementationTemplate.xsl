<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <!-- import the databinding template-->
    <xsl:include href="databindsupporter"/>
    <!-- import the other templates for databinding
         Note  -  these names would be handled by a special
         URI resolver during the xslt transformations
     -->
    <xsl:include href="externalTemplate"/>
    
    
    <xsl:include href="policyExtensionTemplate"/>

    <xsl:template match="/class">
        <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
        <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>
        <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
        <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
        <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
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

        public class <xsl:value-of select="@name"/> extends org.apache.axis2.client.Stub
        <xsl:if test="not(@wrapped)">implements <xsl:value-of select="$interfaceName"/></xsl:if>{
        //default axis home being null forces the system to pick up the mars from the axis2 library
        public static final java.lang.String AXIS2_HOME = null;
        protected static org.apache.axis2.description.AxisOperation[] _operations;
	
	static{

        //creating the Service
        _service = new org.apache.axis2.description.AxisService("<xsl:value-of select="@servicename"/>");	
	<xsl:if test="@policy"> 
	/*
	 * setting the endpont policy
	 */
	 java.lang.String _service_policy_string = "<xsl:value-of select="@policy"/>";
	 org.apache.axis2.description.PolicyInclude servicePolicyInclude 
	 	= _service.getPolicyInclude();
	 servicePolicyInclude.addPolicyElement(
	 		org.apache.axis2.description.PolicyInclude.SERVICE_POLICY, 
	 		getPolicyFromString(_service_policy_string));
	</xsl:if>
	
        //creating the operations
        org.apache.axis2.description.AxisOperation __operation;
	<xsl:if test="//method[@policy]">
	java.lang.String __operation_policy_string;
	</xsl:if>
	
	
        _operations = new org.apache.axis2.description.AxisOperation[<xsl:value-of select="count(method)"/>];
        <xsl:for-each select="method">
            <xsl:choose>
                <xsl:when test="@mep='http://www.w3.org/2004/08/wsdl/in-only'">
                    __operation = new org.apache.axis2.description.OutOnlyAxisOperation();
                </xsl:when>
                <xsl:otherwise>
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                </xsl:otherwise>
            </xsl:choose>

            __operation.setName(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>", "<xsl:value-of select="@name"/>"));
	    
	    <xsl:if test="@policy">
	    __operation_policy_string = "<xsl:value-of select="@policy"/>";
	    org.apache.ws.policy.Policy __operation_policy
	    		= getPolicyFromString(__operation_policy_string);
	    org.apache.axis2.description.PolicyInclude include
	    		= __operation.getPolicyInclude();
	    include.addPolicyElement(org.apache.axis2.description.PolicyInclude.ANON_POLICY,
	    		__operation_policy);
	    
	    </xsl:if>
	    
            _operations[<xsl:value-of select="position()-1"/>]=__operation;
            _service.addOperation(__operation);
        </xsl:for-each>
        }



     public <xsl:value-of select="@name"/>(org.apache.axis2.context.ConfigurationContext configurationContext,
        java.lang.String targetEndpoint)
        throws java.lang.Exception {
	
	<xsl:if test="//@policy">
	
	////////////////////////////////////////////////////////////////////////
		
	org.apache.axis2.engine.AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
	java.util.Collection modules = axisConfiguration.getModules().values();
		
	for (java.util.Iterator iterator = modules.iterator(); iterator.hasNext(); iterator.next()) {
		org.apache.axis2.description.AxisModule axisModule = (org.apache.axis2.description.AxisModule) iterator.next();
		java.lang.String[] namespaces = axisModule.getSupportedPolicyNamespaces();
			
		if (namespaces != null) {
			for (int i = 0; i &lt; namespaces.length; i++) {
				ns2Modules.put(namespaces[i], axisModule);
			}
		}
	}
				
	////////////////////////////////////////////////////////////////////////
		
	</xsl:if>
	
        _serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext,_service);
        _serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(
                targetEndpoint));
        <xsl:if test="$soapVersion='1.2'">
            //Set the soap version
            _serviceClient.getOptions().setSoapVersionURI(org.apache.ws.commons.soap.SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        </xsl:if>
	
	<xsl:if test="//@policy">
	////////////////////////////////////////////////////////////////////////
	
	org.apache.axis2.description.AxisOperation axisOperation;
        
	for (java.util.Iterator iterator = _service.getChildren(); iterator.hasNext(); ){
    		// Engaging the modules per AxisOperation 
		axisOperation = (org.apache.axis2.description.AxisOperation) iterator.next();
		engage(axisOperation, configurationContext.getAxisConfiguration());
	}	
    
	///////////////////////////////////////////////////////////////////////
	</xsl:if>
    }

    /**
     * Default Constructor
     */
    public <xsl:value-of select="@name"/>() throws java.lang.Exception {
        <xsl:for-each select="endpoint">
            <xsl:choose>
                <xsl:when test="position()=1">
                    this("<xsl:value-of select="."/>" );
                </xsl:when>
                <xsl:otherwise>
                    //this("<xsl:value-of select="."/>" );
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    }

    /**
     * Constructor taking the traget endpoint
     */
    public <xsl:value-of select="@name"/>(java.lang.String targetEndpoint) throws java.lang.Exception {
        this(org.apache.axis2.context.ConfigurationContextFactory.createConfigurationContextFromFileSystem(AXIS2_HOME,null),
                targetEndpoint);
    }



        <xsl:for-each select="method">
            <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>
            <xsl:variable name="style"><xsl:value-of select="@style"></xsl:value-of></xsl:variable>
            <xsl:variable name="soapAction"><xsl:value-of select="@soapaction"></xsl:value-of></xsl:variable>

            <xsl:variable name="mep"><xsl:value-of select="@mep"/></xsl:variable>
	    
	    <!-- MTOM -->
	    <xsl:variable name="method-name"><xsl:value-of select="@name"/></xsl:variable>
	    <xsl:variable name="method-ns"><xsl:value-of select="@namespace"/> </xsl:variable>
	    <!-- MTOM -->

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

               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[<xsl:value-of select="position()-1"/>].getName());
              _operationClient.getOptions().setAction("<xsl:value-of select="$soapAction"/>");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              <!--todo if the stub was generated with unwrapping, wrap all parameters into a single element-->

              // create SOAP envelope with that payload
              org.apache.ws.commons.soap.SOAPEnvelope env = null;
                    <xsl:variable name="count"><xsl:value-of select="count(input/param[@type!=''])"></xsl:value-of></xsl:variable>
                    <xsl:choose>
                        <!-- test the number of input parameters
                        If the number of parameter is more then just run the normal test-->
                        <xsl:when test="$count>0">
                            <xsl:choose>
                                <xsl:when test="$style='rpc'">
                                    // Style is RPC
                                    org.apache.axis2.rpc.client.RPCStub.setValueRPC(getFactory(_operationClient.getOptions().getSoapVersionURI(), env,"<xsl:value-of select="@namespace"/>","<xsl:value-of select="@name"/>",
                                    new java.lang.String[]{<xsl:for-each select="input/param[@type!='']"><xsl:if test="position()>1">,</xsl:if>"<xsl:value-of select="@name"/>"</xsl:for-each>},
                                    new java.lang.Object[]{<xsl:for-each select="input/param[@type!='']"><xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@name"/></xsl:for-each>});
                                </xsl:when>
                                <xsl:when test="$style='doc'">
                                    //Style is Doc.
                                    <!-- Let's assume there is only one parameters here -->
                                    <xsl:for-each select="input/param[@location='body']">
                                        <xsl:choose>
                                            <xsl:when test="@type!=''">
                                                 env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), <xsl:value-of select="@name"/>, optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>", "<xsl:value-of select="$method-name"/>")));
                                            </xsl:when>
                                            <xsl:otherwise>
                                                 env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()));
                                            </xsl:otherwise>
                                        </xsl:choose>

                                    </xsl:for-each>	
                                    <xsl:for-each select="input/param[@location='header']">
                                        // add the children only if the parameter is not null
                                        if (<xsl:value-of select="@name"/>!=null){
                                        env.getHeader().addChild(toOM(<xsl:value-of select="@name"/>, optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>", "<xsl:value-of select="$method-name"/>"))));
                                        }
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
                                    org.apache.axis2.rpc.client.RPCStub.setValueRPC(getFactory(_operationClient.getOptions().getSoapVersionURI()), env,"<xsl:value-of select="@namespace"/>","<xsl:value-of select="@name"/>",null,null);
                                </xsl:when>
                                <xsl:when test="$style='doc'">
                                    //Style is Doc. No input parameters
                                    <!-- setValueDoc(env,null); -->
                                </xsl:when>
                                <xsl:otherwise>
                                    //Unknown style!! No code is generated
                                    throw UnsupportedOperationException("Unknown Style");
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>

        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         <xsl:choose>
            <xsl:when test="$outputtype=''">
                return;
            </xsl:when>
            <xsl:otherwise>
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.ws.commons.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                <!-- todo need to change this to cater for unwrapped messages (multiple parts) -->
                <xsl:choose>
                    <xsl:when test="$style='doc'">
                           java.lang.Object object = fromOM(getElement(_returnEnv,"<xsl:value-of select="$style"/>"),<xsl:value-of select="$outputtype"/>.class);
                           return (<xsl:value-of select="$outputtype"/>)object;
                    </xsl:when>
                    <xsl:otherwise>
                        //Unsupported style!! No code is generated
                        throw java.lang.UnsupportedOperationException("Unsupported Style");
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>

                    }
            </xsl:if>
            <!-- Async method generation -->
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

                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[<xsl:value-of select="position()-1"/>].getName());
          _operationClient.getOptions().setAction("<xsl:value-of select="$soapAction"/>");
          _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

          <!--todo if the stub was generated with unwrapping, wrap all parameters into a single element-->

              // create SOAP envelope with that payload
              org.apache.ws.commons.soap.SOAPEnvelope env;
                    <xsl:variable name="count"><xsl:value-of select="count(input/param[@type!=''])"></xsl:value-of></xsl:variable>
                    <xsl:choose>
                        <!-- test the number of input parameters
                        If the number of parameter is more then just run the normal test-->
                        <xsl:when test="$count>0">
                            <xsl:choose>
                                <xsl:when test="$style='rpc'">
                                    // Style is RPC
                                    org.apache.axis2.rpc.client.RPCStub.setValueRPC(getFactory(_operationClient.getOptions().getSoapVersionURI(), env,"<xsl:value-of select="@namespace"/>","<xsl:value-of select="@name"/>",
                                    new java.lang.String[]{<xsl:for-each select="input/param[@type!='']"><xsl:if test="position()>1">,</xsl:if>"<xsl:value-of select="@name"/>"</xsl:for-each>},
                                    new java.lang.Object[]{<xsl:for-each select="input/param[@type!='']"><xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@name"/></xsl:for-each>});
                                </xsl:when>
                                <xsl:when test="$style='doc'">
                                    //Style is Doc.
                                    <xsl:for-each select="input/param[@location='body']">
                                        <xsl:choose>
                                            <xsl:when test="@type!=''">
                                                 env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), <xsl:value-of select="@name"/>, optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>", "<xsl:value-of select="$method-name"/>")));
                                            </xsl:when>
                                            <xsl:otherwise>
                                                 env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()));
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:for-each>
                                    <xsl:for-each select="input/param[@location='header']">
                                         // add the headers only if they are not null
                                        if (<xsl:value-of select="@name"/>!=null){
                                           env.getHeader().addChild(toOM(<xsl:value-of select="@name"/>, optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>", "<xsl:value-of select="$method-name"/>"))));
                                        }
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
                                    org.apache.axis2.rpc.client.RPCStub.setValueRPC(getFactory(_operationClient.getOptions().getSoapVersionURI()), env,"<xsl:value-of select="@namespace"/>","<xsl:value-of select="@name"/>",null,null);
                                </xsl:when>
                                <xsl:when test="$style='doc'">
                                    //Style is Doc. No input parameters
                                    <!-- setValueDoc(env,null); -->
                                </xsl:when>
                                <xsl:otherwise>
                                    //Unknown style!! No code is generated
                                    throw UnsupportedOperationException("Unknown Style");
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>

        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);


                    <xsl:choose>
                        <xsl:when test="$outputtype=''">
                            //Nothing to pass as the callback!!!
                        </xsl:when>
                        <xsl:otherwise>
                           _operationClient.setCallback(new org.apache.axis2.client.async.Callback() {
                    public void onComplete(
                            org.apache.axis2.client.async.AsyncResult result) {
                        java.lang.Object object = fromOM(getElement(
                                result.getResponseEnvelope(), "doc"),
                               <xsl:value-of select="$outputtype"/>.class);
                        callback.receiveResult<xsl:value-of select="@name"/>((<xsl:value-of select="$outputtype"/>) object);
                    }

                    public void onError(java.lang.Exception e) {
                        callback.receiveError<xsl:value-of select="@name"/>(e);
                    }
                });
                        </xsl:otherwise>
                    </xsl:choose>

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[<xsl:value-of select="position()-1"/>].getMessageReceiver()==null &amp;&amp;  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[<xsl:value-of select="position()-1"/>].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(true);

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

                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[<xsl:value-of select="position()-1"/>].getName());
                _operationClient.getOptions().setAction("<xsl:value-of select="$soapAction"/>");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                <xsl:for-each select="input/param[@Action!='']">_operationClient.getOptions().setAction("<xsl:value-of select="@Action"/>");</xsl:for-each>
                org.apache.ws.commons.soap.SOAPEnvelope env;

                <xsl:choose>
                    <!-- test the number of input parameters
                       If the number of parameter is more then just run the normal generation-->
                    <xsl:when test="count(input/param[@type!=''])>0">
                        <xsl:choose>
                            <xsl:when test="$style='rpc'">
                                // Style is RPC
                                env = createEnvelope();
                                org.apache.axis2.rpc.client.RPCStub.setValueRPC(getFactory(_operationClient.getOptions().getSoapVersionURI()), env,"<xsl:value-of select="@namespace"/>","<xsl:value-of select="@name"/>",
                                new java.lang.String[]{<xsl:for-each select="input/param[@type!='']"><xsl:if test="position()>1">,</xsl:if>"<xsl:value-of select="@name"/>"</xsl:for-each>},
                                new java.lang.Object[]{<xsl:for-each select="input/param[@type!='']"><xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@name"/></xsl:for-each>});
                            </xsl:when>
                            <xsl:when test="$style='doc'">
                                <!-- for the doc lit case there can be only one element. So take the first element -->
                                //Style is Doc.
                                env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), <xsl:value-of select="input/param[1]/@name"/>, optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>", "<xsl:value-of select="$method-name"/>")));
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
                                org.apache.axis2.rpc.client.RPCStub.setValueRPC(getFactory(_operationClient.getOptions().getSoapVersionURI()), env,"<xsl:value-of select="@namespace"/>","<xsl:value-of select="@name"/>",null,null);
                            </xsl:when>
                            <xsl:when test="$style='doc'">
                                //Style is Doc. No input parameters
                                <!-- setValueDoc(env,null); -->
                            </xsl:when>
                            <xsl:otherwise>
                                //Unknown style!! No code is generated
                                throw UnsupportedOperationException("Unknown Style");
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>

                // create message context with that soap envelope
            org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
            _messageContext.setEnvelope(env);

            // add the message contxt to the operation client
            _operationClient.addMessageContext(_messageContext);

             _operationClient.execute(true);
             return;
           }
            </xsl:if>
        </xsl:for-each>
	
	<xsl:if test="//@policy">
	
	/** */
	private java.util.HashMap ns2Modules = new java.util.HashMap();
	
	////////////////////////////////////////////////////////////////////////
	
	private static org.apache.ws.policy.Policy getPolicyFromString (java.lang.String policyString) {
		org.apache.ws.policy.util.PolicyReader prdr 
			= org.apache.ws.policy.util.PolicyFactory.getPolicyReader(
					org.apache.ws.policy.util.PolicyFactory.OM_POLICY_READER);
		try {
			if (policyString != null 
				&amp;&amp; !policyString.trim().equals("")) {
					return prdr.readPolicy(
						new java.io.ByteArrayInputStream(
						policyString.getBytes()));
			}
		
		} catch (Exception e) {
			throw new RuntimeException(
				"cannot convert "+ policyString	+ " to policy", e);
		}
		return null;
	}
	
	private static org.apache.ws.policy.Policy merge(java.lang.String policyString1,
		java.lang.String policyString2) {
		return (org.apache.ws.policy.Policy) getPolicyFromString(policyString1)
			.merge(getPolicyFromString(policyString2));
	}
	
	// /////////////////////////////////////////////////////////////////
	
	private java.util.ArrayList getModules(java.util.List termsList) {
		java.util.ArrayList arrayList = new java.util.ArrayList();
		java.util.Iterator iterator = termsList.iterator();
		
		org.apache.ws.policy.PrimitiveAssertion pa;
		java.lang.String namespace;
		org.apache.axis2.description.AxisModule axisModule;
		
		while (iterator.hasNext()) {
			pa = (org.apache.ws.policy.PrimitiveAssertion) iterator.next();
			namespace = pa.getName().getNamespaceURI();
			axisModule = (org.apache.axis2.description.AxisModule) ns2Modules.get(namespace);
			
			if (axisModule == null) {
				// TODO
				System.err.println("Warning: cannot find a module for process PrimitiveAssertion - " + pa.getName());
			}			
			arrayList.add(axisModule);
		}
		
		return arrayList;
	}
	private void engage(org.apache.axis2.description.AxisDescription axisDescription, org.apache.axis2.engine.AxisConfiguration axisConfiguration) throws org.apache.axis2.AxisFault {
		
		org.apache.axis2.description.PolicyInclude policyInclude = axisDescription.getPolicyInclude();
		org.apache.ws.policy.Policy policy = policyInclude.getEffectivePolicy();
		
		if (policy == null) {
			return;
		}
		
		if (! policy.isNormalized()) {
			policy = (org.apache.ws.policy.Policy) policy.normalize();
		}
		
		org.apache.ws.policy.XorCompositeAssertion xor = (org.apache.ws.policy.XorCompositeAssertion) policy.getTerms().get(0);
		if (xor.isEmpty()) {
			// TODO
			throw new RuntimeException("No policy alternative found");
		}
		org.apache.ws.policy.AndCompositeAssertion anAlternative = (org.apache.ws.policy.AndCompositeAssertion) xor.getTerms().get(0);
		java.util.List moduleList = getModules(anAlternative.getTerms());
		
		if (axisDescription instanceof org.apache.axis2.description.AxisService) {
			for (java.util.Iterator iterator = moduleList.iterator(); iterator.hasNext();) {
				((org.apache.axis2.description.AxisService) axisDescription).engageModule((org.apache.axis2.description.AxisModule) iterator.next(), axisConfiguration);
			}
		} else if (axisDescription instanceof org.apache.axis2.description.AxisOperation) {
			for (java.util.Iterator iterator = moduleList.iterator(); iterator.hasNext();) {
				((org.apache.axis2.description.AxisOperation) axisDescription).engageModule((org.apache.axis2.description.AxisModule) iterator.next(), axisConfiguration);
			}
		}
	}

	
	////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	
	
	
	
	
	</xsl:if>
	
	///////////////////////////////////////////////////////////////////////
	
	
	
		private javax.xml.namespace.QName[] opNameArray;
		
		
	private boolean optimizeContent(javax.xml.namespace.QName opName) {
		if (opNameArray == null) {
			return false;
		}
		for (int i = 0; i &lt; opNameArray.length; i++) {
			if (opName.equals(opNameArray[i])) {
				return true;   
			}
		}
		return false;
	}
	
	
	
	////////////////////////////////////////////////////////////////////////


        //<xsl:apply-templates/>

        }



    </xsl:template>


</xsl:stylesheet>
