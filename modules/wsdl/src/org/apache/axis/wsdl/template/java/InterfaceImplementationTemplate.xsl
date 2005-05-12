<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
    <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
    <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>
    package <xsl:value-of select="$package"/>;

    /*
     *  Auto generated java implementation by the Axis code generator
    */

    public class <xsl:value-of select="@name"></xsl:value-of> extends org.apache.axis.clientapi.Stub implements <xsl:value-of select="$interfaceName"/>{

       static{
        _axisGlobal = new org.apache.axis.description.AxisGlobal();
        _axisSystem = new org.apache.axis.engine.AxisSystemImpl(_axisGlobal);
		_systemContext = new org.apache.axis.context.SystemContext(_axisSystem);
		_service = new org.apache.axis.description.AxisService();

        _service.setName(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>", "<xsl:value-of select="@servicename"/>"));

        org.apache.axis.description.AxisOperation __operation;

      <xsl:for-each select="method">
          __operation = new org.apache.axis.description.AxisOperation();
          __operation.setName(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>", "<xsl:value-of select="@name"/>"));
          // more things are supposed to come here

          _operations[<xsl:value-of select="position()-1"/>]=__operation;

     </xsl:for-each>
       }
     <xsl:for-each select="method">
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>
         <xsl:variable name="inputtype"><xsl:value-of select="input/param/@type"></xsl:value-of></xsl:variable>  <!-- this needs to change-->
         <xsl:variable name="inputparam"><xsl:value-of select="input/param/@name"></xsl:value-of></xsl:variable>  <!-- this needs to change-->

        /**                                                                               
         * Auto generated method signature
         * @see <xsl:value-of select="$package"/>.<xsl:value-of select="$interfaceName"/>#<xsl:value-of select="@name"/>
         *<xsl:if test="$inputtype!=''">@param <xsl:value-of select="$inputparam"></xsl:value-of></xsl:if>
         */
        public  <xsl:value-of select="$outputtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>(<xsl:if test="$inputtype!=''"><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="$inputparam"></xsl:value-of></xsl:if>) throws java.rmi.RemoteException;
            // we know its call because we have the mep at the time of the stub generation.
		    Call _call = new Call(_systemContext.getServiceContext("<xsl:value-of select="generate-id()"/>"));<!-- this needs to change -->
 		    MessageContext _messageContext = getMessageContext();
		    _call.invokeNonBlocking(_operations[<xsl:value-of select="position()-1"/>], _messageContext);
            <xsl:if test="$outputtype!=''">return null;</xsl:if>  <!-- this needs to be changed -->
        }
     </xsl:for-each>
    }
    </xsl:template>
 </xsl:stylesheet>