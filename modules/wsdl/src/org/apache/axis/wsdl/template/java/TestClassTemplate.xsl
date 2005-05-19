<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
    <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
    <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>
    <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
    <xsl:variable name="stubname"><xsl:value-of select="@stubname"/></xsl:variable>
    <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
    <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
    package <xsl:value-of select="$package"/>;

    /*
     *  Auto generated Junit test case by the Axis code generator
    */

    public class <xsl:value-of select="@name"/> extends junit.framework.TestCase{


     <xsl:for-each select="method">

         <xsl:if test="$isSync='1'">

        /**
         * Auto generated test method
         */
        public  void test<xsl:value-of select="@name"/>() throws java.lang.Exception{

        <xsl:value-of select="$stubname"/> stub = new <xsl:value-of select="$package"/>.<xsl:value-of select="$stubname"/>();
         assertNotNull(stub.<xsl:value-of select="@name"/>(getRPCStyleChildElement("<xsl:value-of select="@namespace"/>","<xsl:value-of select="@name"/>")));


        }
        </xsl:if>
        <xsl:if test="$isAsync='1'">
            <xsl:variable name="tempCallbackName">tempCallback<xsl:value-of select="generate-id()"/></xsl:variable>
         /**
         * Auto generated test method
         */
        public  void testStart<xsl:value-of select="@name"/>() throws java.lang.Exception{
            <xsl:value-of select="$stubname"/> stub = new <xsl:value-of select="$package"/>.<xsl:value-of select="$stubname"/>();
            stub.start<xsl:value-of select="@name"/>(
                    getRPCStyleChildElement("<xsl:value-of select="@namespace"/>","<xsl:value-of select="@name"/>"),
                    new <xsl:value-of select="$tempCallbackName"/>()
            );
        }

        private class <xsl:value-of select="$tempCallbackName"/>  extends <xsl:value-of select="$package"/>.<xsl:value-of select="$callbackname"/>{
            public <xsl:value-of select="$tempCallbackName"/>(){ super(null);}

            public void receiveResult<xsl:value-of select="@name"/>(org.apache.axis.clientapi.AsyncResult result) {
			    assertNotNull(result);
            }

            public void receiveError<xsl:value-of select="@name"/>(java.lang.Exception e) {
                fail();
            }

        }
      </xsl:if>
     </xsl:for-each>

       private org.apache.axis.om.OMElement getRPCStyleChildElement(String methodNamespaceURI,String methodName){
         org.apache.axis.om.OMFactory omFactory = org.apache.axis.om.OMAbstractFactory.getOMFactory();
         org.apache.axis.om.OMNamespace ns = omFactory.createOMNamespace(methodNamespaceURI,"ns");
         org.apache.axis.om.OMNamespace emptyNS = omFactory.createOMNamespace("",null);
         org.apache.axis.om.OMNamespace soapEnvNs = omFactory.createOMNamespace(
                org.apache.axis.soap.impl.llom.soap11.SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                org.apache.axis.soap.impl.llom.soap11.SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX) ;
        org.apache.axis.om.OMElement rootElt =  omFactory.createOMElement(methodName,ns);
        rootElt.declareNamespace(soapEnvNs);
        rootElt.addAttribute("encodingStyle","http://schemas.xmlsoap.org/soap/encoding/",soapEnvNs);

        //have to add the parameters here

        return rootElt;
       }
    }
    </xsl:template>
 </xsl:stylesheet>