<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
    <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
    <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>
    <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
    <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
    <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
    namespace <xsl:value-of select="$package"/>{

    /// &lt;summary&gt;
    /// Auto generated C# implementation by the Axis code generator
    /// This is meant to be used with the IKVM converted Axis libraries
    ///&lt;/summary&gt;

    public class <xsl:value-of select="@name"/> :org.apache.axis2.client.Stub,<xsl:value-of select="$interfaceName"/>{

        private static org.apache.axis2.description.AxisGlobal  _axisGlobal = null;
        private static org.apache.axis2.engine.AxisSystemImpl   _axisSystem = null;
        private static org.apache.axis2.context.SystemContext   _systemContext = null;
        private static org.apache.axis2.description.AxisService   _service = null;
        private static org.apache.axis2.description.AxisOperation[] _operations = new org.apache.axis2.description.AxisOperation[<xsl:value-of select="count(method)"/>];

       ///&lt;summary&gt;
       /// static constructor
       ///&lt; /summary &gt;
       static <xsl:value-of select="@name"/>(){
         _axisGlobal = new org.apache.axis2.description.AxisGlobal();
         _axisSystem = new org.apache.axis2.engine.AxisSystemImpl(_axisGlobal);
		 _systemContext = new org.apache.axis2.context.SystemContext(_axisSystem);
		 _service = new org.apache.axis2.description.AxisService();
         _service.setName(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>", "<xsl:value-of select="@servicename"/>"));
         org.apache.axis2.description.AxisOperation __operation;

      <xsl:for-each select="method">
          __operation = new org.apache.axis2.description.AxisOperation();
          __operation.setName(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>", "<xsl:value-of select="@name"/>"));
          // more things are supposed to come here
          _operations[<xsl:value-of select="position()-1"/>]=__operation;
     </xsl:for-each>
       }
     <xsl:for-each select="method">
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>
         <xsl:variable name="inputtype"><xsl:value-of select="input/param/@type"></xsl:value-of></xsl:variable>  <!-- this needs to change-->
         <xsl:variable name="inputparam"><xsl:value-of select="input/param/@name"></xsl:value-of></xsl:variable>  <!-- this needs to change-->

         <xsl:if test="$isSync='1'">
        /// &lt;summary&gt;
        /// Auto generated method signature
        ///&lt;/summary&gt;
        ///<xsl:if test="$inputtype!=''">&lt;param name="<xsl:value-of select="$inputparam"/>"/&gt;</xsl:if>
        ///
        public  <xsl:if test="$outputtype=''">void</xsl:if><xsl:if test="$outputtype!=''"><xsl:value-of select="$outputtype"/></xsl:if><xsl:text> </xsl:text><xsl:value-of select="@name"/>(<xsl:if test="$inputtype!=''"><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="$inputparam"></xsl:value-of></xsl:if>)
        {

		    org.apache.axis2.client.Call _call = new org.apache.axis2.client.Call(_systemContext.getServiceContext("<xsl:value-of select="generate-id()"/>"));<!-- this needs to change -->
 		    org.apache.axis2.context.MessageContext _messageContext = getMessageContext();
             <xsl:if test="$outputtype=''">
             _call.invokeBlocking(_operations[<xsl:value-of select="position()-1"/>], _messageContext);
             return null;
             </xsl:if>
             <xsl:if test="$outputtype!=''">
             Object obj = _call.invokeBlocking(_operations[<xsl:value-of select="position()-1"/>], _messageContext);
             return (<xsl:value-of select="$outputtype"/>)obj;
            </xsl:if>
            <!-- this needs to be changed -->
        }
        </xsl:if>
        <xsl:if test="$isAsync='1'">
        ///&lt;summary&gt;
        ///Auto generated method signature
        ///&lt;/summary&gt;
        ///<xsl:if test="$inputtype!=''">&lt;param name="<xsl:value-of select="$inputparam"/>"/&gt;</xsl:if>
        ///
        public void start<xsl:value-of select="@name"/>(<xsl:if test="$inputtype!=''"><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="$inputparam"></xsl:value-of></xsl:if>)
        {
                // we know its call because we have the mep at the time of the stub generation.
		        org.apache.axis2.client.Call _call = new org.apache.axis2.client.Call(_systemContext.getServiceContext("<xsl:value-of select="generate-id()"/>"));<!-- this needs to change -->
 		        org.apache.axis2.context.MessageContext _messageContext = getMessageContext();
		        _call.invokeNonBlocking(_operations[<xsl:value-of select="position()-1"/>], _messageContext, new CallBack<xsl:value-of select="generate-id()"/>());
            <!-- this needs to be changed -->
        }

        ///&lt;summary&gt;
        ///  C# does not support anoynmous inner classes
        ///&lt;/summary&gt;
        private class CallBack<xsl:value-of select="generate-id()"/>:org.apache.axis2.client.CallBack
        {
          public void onComplete(org.apache.axis2.client.async.AsyncResult result)
           {
                    new <xsl:value-of select="$package"/>.<xsl:value-of select="$callbackname"/>().receiveResult<xsl:value-of select="@name"/>(result);
           }
           public void reportError(java.lang.Exception e)
           {
                     new <xsl:value-of select="$package"/>.<xsl:value-of select="$callbackname"/>().receiveError<xsl:value-of select="@name"/>(e);
           }



        }
      </xsl:if>
     </xsl:for-each>
    }
    }
    </xsl:template>
 </xsl:stylesheet>