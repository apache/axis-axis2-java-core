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
        static{
          org.apache.axis.description.OperationDescription __operation;
      <xsl:for-each select="method">
          __operation = new org.apache.axis.description.OperationDescription();
          __operation.setName(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>", "<xsl:value-of select="@name"/>"));
          _operations[<xsl:value-of select="position()-1"/>]=__operation;
     </xsl:for-each>
       }

       /**
        * Constructor
        */
        public <xsl:value-of select="@name"/>(String axis2Home) throws java.lang.Exception {
		    super(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>","<xsl:value-of select="@servicename"/>"),axis2Home);
	    }

        /**
        * Default Constructor
        */
        public <xsl:value-of select="@name"/>() throws java.lang.Exception {
		    this(AXIS2_HOME);
	    }


    public void _setSessionInfo(Object key, Object value)throws java.lang.Exception{
		if(!_maintainSession){
			//TODO Comeup with a Exception
			throw new java.lang.Exception("Client is running the session OFF mode: Start session before saving to a session ");
		}
		_configurationContext.getServiceContext(_currentSessionId).setProperty(key, value);
	}


	public Object _getSessionInfo(Object key) throws java.lang.Exception{
		if(!_maintainSession){
			//TODO Comeup with a Exception
			throw new java.lang.Exception("Client is running the session OFF mode: Start session before saving to a session ");
		}
		return _configurationContext.getServiceContext(_currentSessionId).getProperty(key);
	}

    /**
     * get the message context
     */
    private org.apache.axis.context.MessageContext _getMessageContext(){
            return null;
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

		    org.apache.axis.clientapi.Call _call = new org.apache.axis.clientapi.Call(_configurationContext.getServiceContext(_getServiceContextID()));<!-- this needs to change -->
 		    org.apache.axis.context.MessageContext _messageContext = _getMessageContext();
             <xsl:if test="$outputtype=''">
             _call.invokeBlocking(_operations[<xsl:value-of select="position()-1"/>], _messageContext);
             return;
             </xsl:if>
             <xsl:if test="$outputtype!=''">
             Object obj = _call.invokeBlocking(_operations[<xsl:value-of select="position()-1"/>], _messageContext);
             return (<xsl:value-of select="$outputtype"/>)obj;
            </xsl:if>
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
                // we know its call because we have the mep at the time of the stub generation.
		        org.apache.axis.clientapi.Call _call = new org.apache.axis.clientapi.Call(_configurationContext.getServiceContext(_getServiceContextID()));<!-- this needs to change -->
 		        org.apache.axis.context.MessageContext _messageContext = _getMessageContext();
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