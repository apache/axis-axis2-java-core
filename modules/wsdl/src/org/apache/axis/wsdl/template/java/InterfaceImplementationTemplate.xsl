<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
    <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
    <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>
    <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
    <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
    <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
    package <xsl:value-of select="$package"/>;

    /*
     *  Auto generated java implementation by the Axis code generator
    */

    public class <xsl:value-of select="@name"/> extends org.apache.axis.clientapi.Stub implements <xsl:value-of select="$interfaceName"/>{
        public static final String AXIS2_HOME = ".";
        protected static org.apache.axis.description.OperationDescription[] _operations;

        static{

           //creating the Service
           _service = new org.apache.axis.description.ServiceDescription(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>","<xsl:value-of select="@servicename"/>"));

           //creating the operations
           org.apache.axis.description.OperationDescription __operation;
           _operations = new org.apache.axis.description.OperationDescription[<xsl:value-of select="count(method)"/>];
      <xsl:for-each select="method">
          __operation = new org.apache.axis.description.OperationDescription();
          __operation.setName(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>", "<xsl:value-of select="@name"/>"));
          _operations[<xsl:value-of select="position()-1"/>]=__operation;
          _service.addOperation(__operation);
     </xsl:for-each>
       }

       /**
        * Constructor
        */
        public <xsl:value-of select="@name"/>(String axis2Home,String targetEndpoint) throws java.lang.Exception {

           this.toEPR = new org.apache.axis.addressing.EndpointReference(org.apache.axis.addressing.AddressingConstants.WSA_TO,targetEndpoint);
		    //creating the configuration
           _configurationContext = new org.apache.axis.context.ConfigurationContextFactory().buildClientEngineContext(axis2Home);
            _configurationContext.getEngineConfig().addService(_service);
           _serviceContext = _configurationContext.createServiceContext(_service.getName());

	    }

        /**
        * Default Constructor
        */
        public <xsl:value-of select="@name"/>() throws java.lang.Exception {
		    this(AXIS2_HOME,"<xsl:value-of select="endpoint"/>" );
	    }



     <xsl:for-each select="method">
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>
         <xsl:variable name="inputtype"><xsl:value-of select="input/param/@type"></xsl:value-of></xsl:variable>  <!-- this needs to change-->
         <xsl:variable name="inputparam"><xsl:value-of select="input/param/@name"></xsl:value-of></xsl:variable>  <!-- this needs to change-->

         <!-- When genrating code, the MEP should be taken into account    -->

         <xsl:if test="$isSync='1'">
        /**
         * Auto generated method signature
         * @see <xsl:value-of select="$package"/>.<xsl:value-of select="$interfaceName"/>#<xsl:value-of select="@name"/>
         *<xsl:if test="$inputtype!=''">@param <xsl:value-of select="$inputparam"></xsl:value-of></xsl:if>
         */
        public  <xsl:if test="$outputtype=''">void</xsl:if><xsl:if test="$outputtype!=''"><xsl:value-of select="$outputtype"/></xsl:if><xsl:text> </xsl:text><xsl:value-of select="@name"/>(<xsl:if test="$inputtype!=''"><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="$inputparam"></xsl:value-of></xsl:if>) throws java.rmi.RemoteException{

		    org.apache.axis.clientapi.Call _call = new org.apache.axis.clientapi.Call(_serviceContext);
 		    org.apache.axis.context.MessageContext _messageContext = getMessageContext();
            _call.setTo(toEPR);
            org.apache.axis.soap.SOAPEnvelope env = null;
            env = createEnvelope();
             <xsl:choose>
              <xsl:when test="$inputtype!=''">
                setValueRPC(env,
                            "<xsl:value-of select="@namespace"/>",
                            "<xsl:value-of select="@name"/>",
                            new String[]{"<xsl:value-of select="$inputparam"/>"},
                            new Object[]{<xsl:value-of select="$inputparam"/>});     //fix this - code is RPC only
              </xsl:when>
              <xsl:otherwise>
               setValueRPC(env,
                            "<xsl:value-of select="@namespace"/>",
                            "<xsl:value-of select="@name"/>",
                            null,
                            null);     //fix this - code is RPC only
             </xsl:otherwise>
            </xsl:choose>
             _messageContext.setEnvelope(env);
             <xsl:choose>
                 <xsl:when test="$outputtype=''">
                    _call.invokeBlocking(_operations[<xsl:value-of select="position()-1"/>], _messageContext);
                    return;
                 </xsl:when>
                 <xsl:otherwise>
             org.apache.axis.context.MessageContext  _returnMessageContext = _call.invokeBlocking(_operations[<xsl:value-of select="position()-1"/>], _messageContext);
             org.apache.axis.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
             <!-- todo this needs to be fixed -->
             return (<xsl:value-of select="$outputtype"/>)getValue(_returnEnv,"rpc",<xsl:value-of select="$outputtype"/>.class);
                 </xsl:otherwise>
             </xsl:choose>

            <!-- this needs to be changed -->
        }
        </xsl:if>
        <xsl:if test="$isAsync='1'">
         /**
         * Auto generated method signature
         * @see <xsl:value-of select="$package"/>.<xsl:value-of select="$interfaceName"/>#start<xsl:value-of select="@name"/>
         *<xsl:if test="$inputtype!=''">@param <xsl:value-of select="$inputparam"></xsl:value-of></xsl:if>
         */
        public  void start<xsl:value-of select="@name"/>(<xsl:if test="$inputtype!=''"><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="$inputparam"></xsl:value-of>,</xsl:if>final <xsl:value-of select="$package"/>.<xsl:value-of select="$callbackname"/> callback) throws java.rmi.RemoteException{
             org.apache.axis.clientapi.Call _call = new org.apache.axis.clientapi.Call(_serviceContext);<!-- this needs to change -->
 		     org.apache.axis.context.MessageContext _messageContext = getMessageContext();
             _call.setTo(toEPR);
             org.apache.axis.soap.SOAPEnvelope env = null;
             <xsl:choose>
              <xsl:when test="$inputtype!=''">
                env = createEnvelope();
                setValueRPC(env,
                            "<xsl:value-of select="@namespace"/>",
                            "<xsl:value-of select="@name"/>",
                            new String[]{"<xsl:value-of select="$inputparam"/>"},
                            new Object[]{<xsl:value-of select="$inputparam"/>});
              </xsl:when>
              <xsl:otherwise>
                env = createEnvelope();
                setValueRPC(env,
                            "<xsl:value-of select="@namespace"/>",
                            "<xsl:value-of select="@name"/>",
                            null,
                            null);
             </xsl:otherwise>
            </xsl:choose>
             _messageContext.setEnvelope(env);
		      _call.invokeNonBlocking(_operations[<xsl:value-of select="position()-1"/>], _messageContext, new org.apache.axis.clientapi.Callback(){
                   public void onComplete(org.apache.axis.clientapi.AsyncResult result){
                         callback.receiveResult<xsl:value-of select="@name"/>(result);
                   }
                   public void reportError(java.lang.Exception e){
                         callback.receiveError<xsl:value-of select="@name"/>(e);
                   }

              }
            );

            <!-- this needs to be changed -->
        }
      </xsl:if>
     </xsl:for-each>
    }
    </xsl:template>
 </xsl:stylesheet>