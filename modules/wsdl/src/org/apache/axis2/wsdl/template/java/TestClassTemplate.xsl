<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
    <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
    <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>
    <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
    <xsl:variable name="stubname"><xsl:value-of select="@stubname"/></xsl:variable>
    <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
    <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
    <xsl:variable name="dbpackage"><xsl:value-of select="@dbsupportpackage"/></xsl:variable>

    package <xsl:value-of select="$package"/>;
    
	import java.io.InputStream;
	import java.net.ServerSocket;

	import javax.xml.namespace.QName;
	
	import org.apache.axis2.context.ConfigurationContext;
	import org.apache.axis2.deployment.DeploymentEngine;
	import org.apache.axis2.description.ServiceDescription;
	import org.apache.axis2.engine.AxisConfiguration;
	import org.apache.axis2.wsdl.codegen.Constants;
	import org.apache.axis2.om.OMAbstractFactory;
	import org.apache.axis2.om.OMElement;
	import org.apache.axis2.om.OMFactory;
	import org.apache.axis2.om.impl.llom.OMTextImpl;
	import org.apache.axis2.transport.http.SimpleHTTPServer;


    /*
     *  Auto generated Junit test case by the Axis code generator
    */

    public class <xsl:value-of select="@name"/> extends junit.framework.TestCase{

     <xsl:for-each select="method">
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>
         <xsl:variable name="inputtype"><xsl:value-of select="input/param/@type"></xsl:value-of></xsl:variable>  <!-- this needs to change-->
         <xsl:variable name="inputparam"><xsl:value-of select="input/param/@name"></xsl:value-of></xsl:variable>  <!-- this needs to change-->
         <xsl:variable name="dbsupportclassname"><xsl:value-of select="@dbsupportname"></xsl:value-of></xsl:variable>
         <xsl:if test="$isSync='1'">

        /**
         * Auto generated test method
         */
        public  void test<xsl:value-of select="@name"/>() throws java.lang.Exception{

        <xsl:value-of select="$package"/>.<xsl:value-of select="$stubname"/> stub =
                    new <xsl:value-of select="$package"/>.<xsl:value-of select="$stubname"/>();//the default implementation should point to the right endpoint
          //create a new databinder
        <xsl:variable name="fullsupporterclassname"><xsl:value-of select="$dbpackage"/>.<xsl:value-of select="$dbsupportclassname"/></xsl:variable>
        <xsl:value-of select="$fullsupporterclassname"/> databindSupporter = new <xsl:value-of select="$fullsupporterclassname"/>();

           <xsl:choose>
             <xsl:when test="$inputtype!=''">
                <xsl:choose>
                    <xsl:when test="$outputtype=''">
                    //There is no output to be tested!
                    stub.<xsl:value-of select="@name"/>((<xsl:value-of select="$inputtype"/>)databindSupporter.getTestObject(<xsl:value-of select="$inputtype"/>.class));
                    </xsl:when>
                    <xsl:otherwise>
                    assertNotNull(stub.<xsl:value-of select="@name"/>((<xsl:value-of select="$inputtype"/>)databindSupporter.getTestObject(<xsl:value-of select="$inputtype"/>.class)));
                    </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                  <xsl:choose>
                    <xsl:when test="$outputtype=''">
                    //There is no output to be tested!
                    stub.<xsl:value-of select="@name"/>();
                    </xsl:when>
                    <xsl:otherwise>
                    assertNotNull(stub.<xsl:value-of select="@name"/>());
                    </xsl:otherwise>
                </xsl:choose>
             </xsl:otherwise>
            </xsl:choose>



        }
        </xsl:if>
        <xsl:if test="$isAsync='1'">
            <xsl:variable name="tempCallbackName">tempCallback<xsl:value-of select="generate-id()"/></xsl:variable>
         /**
         * Auto generated test method
         */
        public  void testStart<xsl:value-of select="@name"/>() throws java.lang.Exception{
            <xsl:value-of select="$package"/>.<xsl:value-of select="$stubname"/> stub = new <xsl:value-of select="$package"/>.<xsl:value-of select="$stubname"/>();
             //create a new databinder
            <xsl:variable name="fullsupporterclassname"><xsl:value-of select="$dbpackage"/>.<xsl:value-of select="$dbsupportclassname"/></xsl:variable>
            <xsl:value-of select="$fullsupporterclassname"/> databindSupporter = new <xsl:value-of select="$fullsupporterclassname"/>();
             <xsl:choose>
             <xsl:when test="$inputtype!=''">
                stub.start<xsl:value-of select="@name"/>((<xsl:value-of select="$inputtype"/>)databindSupporter.getTestObject(<xsl:value-of select="$inputtype"/>.class),
                    new <xsl:value-of select="$tempCallbackName"/>()
                );
              </xsl:when>
              <xsl:otherwise>
                stub.start<xsl:value-of select="@name"/>(
                    new <xsl:value-of select="$tempCallbackName"/>()
                );
             </xsl:otherwise>
            </xsl:choose>


        }

        private class <xsl:value-of select="$tempCallbackName"/>  extends <xsl:value-of select="$package"/>.<xsl:value-of select="$callbackname"/>{
            public <xsl:value-of select="$tempCallbackName"/>(){ super(null);}

            public void receiveResult<xsl:value-of select="@name"/>(org.apache.axis2.clientapi.AsyncResult result) {
			    assertNotNull(result.getResponseEnvelope().getBody().getFirstChild());
            }

            public void receiveError<xsl:value-of select="@name"/>(java.lang.Exception e) {
                fail();
            }

        }
      </xsl:if>
     </xsl:for-each>


     public static Object createTestInput(Class paramClass){

      OMFactory factory = OMAbstractFactory.getOMFactory();
		OMElement element = factory.createOMElement(new QName("http://soapinterop.org/", "<xsl:value-of select="generate-id()"/>"), null);
		OMElement element1 = factory.createOMElement(new QName("http://soapinterop.org/","<xsl:value-of select="generate-id()"/>"), element);
		element.addChild(element1);
    	OMTextImpl text = new OMTextImpl("<xsl:value-of select="generate-id()"/>");
    	element1.addChild(text);
    	return element;
    }
    }
    </xsl:template>
 </xsl:stylesheet>