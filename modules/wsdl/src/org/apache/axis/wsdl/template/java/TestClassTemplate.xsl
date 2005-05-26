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
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>
         <xsl:variable name="inputtype"><xsl:value-of select="input/param/@type"></xsl:value-of></xsl:variable>  <!-- this needs to change-->
         <xsl:variable name="inputparam"><xsl:value-of select="input/param/@name"></xsl:value-of></xsl:variable>  <!-- this needs to change-->
         <xsl:if test="$isSync='1'">

        /**
         * Auto generated test method
         */
        public  void test<xsl:value-of select="@name"/>() throws java.lang.Exception{

        <xsl:value-of select="$stubname"/> stub = new <xsl:value-of select="$package"/>.<xsl:value-of select="$stubname"/>();
           <xsl:choose>
             <xsl:when test="$inputtype!=''">
               assertNotNull(stub.<xsl:value-of select="@name"/>(
                                (<xsl:value-of select="$inputtype"/>)createTestInput(<xsl:value-of select="$inputtype"/>.class)));//this should come as a type
              </xsl:when>
              <xsl:otherwise>
                assertNotNull(stub.<xsl:value-of select="@name"/>());
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
            <xsl:value-of select="$stubname"/> stub = new <xsl:value-of select="$package"/>.<xsl:value-of select="$stubname"/>();
             <xsl:choose>
             <xsl:when test="$inputtype!=''">
                stub.start<xsl:value-of select="@name"/>(
                   (<xsl:value-of select="$inputtype"/>)createTestInput(<xsl:value-of select="$inputtype"/>.class),
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

            public void receiveResult<xsl:value-of select="@name"/>(org.apache.axis.clientapi.AsyncResult result) {
			    assertNotNull(result);
            }

            public void receiveError<xsl:value-of select="@name"/>(java.lang.Exception e) {
                fail();
            }

        }
      </xsl:if>
     </xsl:for-each>


     public static Object createTestInput(Class paramClass){

       if (paramClass.equals(String.class)){
           return new String("Test");
       }else if (paramClass.equals(Integer.class)){
            return new Integer(1);
       }else if (paramClass.equals(Float.class)){
           return new Float(2);
       }else if (paramClass.equals(Double.class)){
           return new Double(3);
       //todo this seems to be a long list... needs to complete this
       //}else if (paramClass.equals(OMElement.class)){
       //  return null;
       }else{
         return new Object();
       }

    }
    }
    </xsl:template>
 </xsl:stylesheet>