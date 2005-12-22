<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/interface">
        <xsl:variable name="skeletonname"><xsl:value-of select="@skeletonname"/></xsl:variable>
        <xsl:variable name="dbsupportpackage"><xsl:value-of select="@dbsupportpackage"/></xsl:variable>

        /**
         * <xsl:value-of select="@name"/>.java
         *
         * This file was auto-generated from WSDL
         * by the Apache Axis2 version: #axisVersion# #today#
         */
        package <xsl:value-of select="@package"/>;

        /**
         *  <xsl:value-of select="@name"/> message receiver
         */

        public class <xsl:value-of select="@name"/> extends <xsl:value-of select="@basereceiver"/>{

        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
        throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(msgContext);

        //Inject the Message Context if it is asked for
        org.apache.axis2.engine.DependencyManager.configureBusinessLogicProvider(obj, msgContext, newMsgContext);

        <xsl:value-of select="$skeletonname"/> skel = (<xsl:value-of select="$skeletonname"/>)obj;
        //Out Envelop
        org.apache.axis2.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        String methodName;
        if(op.getName() != null &amp; (methodName = op.getName().getLocalPart()) != null){

        <xsl:for-each select="method">

            <xsl:variable name="returntype"><xsl:value-of select="output/param/@type"/></xsl:variable>
            <xsl:variable name="returnvariable"><xsl:value-of select="output/param/@name"/></xsl:variable>
            <xsl:variable name="namespace"><xsl:value-of select="@namespace"/></xsl:variable>

            <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
            <xsl:variable name="style"><xsl:value-of select="@style"/></xsl:variable>


            if("<xsl:value-of select="@name"/>".equals(methodName)){


            <xsl:if test="$returntype!=''">
                <xsl:value-of select="$returntype"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="$returnvariable"/> = null;
            </xsl:if>


            <xsl:choose>
                <xsl:when test="$style='rpc'">

                    //rpc style  -- this needs to be filled

                </xsl:when>
                <xsl:when test="$style='doc'">
                    //doc style
                    <xsl:if test="$returntype!=''"><xsl:value-of select="$returnvariable"/> =</xsl:if>
                    <xsl:variable name="paramCount"> <xsl:value-of select="count(input/param[@location='body'])"/></xsl:variable>
                    <xsl:choose>
                        <xsl:when test="$paramCount &gt; 0"> skel.<xsl:value-of select="@name"/>(
                            <xsl:for-each select="input/param[@location='body']">
                                <xsl:if test="@type!=''">(<xsl:value-of select="@type"/>)fromOM((org.apache.axis2.om.OMElement)msgContext.getEnvelope().getBody().getFirstElement().detach(), <xsl:value-of select="@type"/>.class)<xsl:if test="position() &gt; 1">,</xsl:if></xsl:if>
                            </xsl:for-each>);
                        </xsl:when>
                        <xsl:otherwise>skel.<xsl:value-of select="@name"/>();</xsl:otherwise>
                    </xsl:choose>


                    //Create a default envelop
                    envelope = getSOAPFactory().getDefaultEnvelope();
                    //Create a Omelement of the result if a result exist

                    <xsl:if test="$returntype!=''">envelope.getBody().setFirstChild(toOM(<xsl:value-of select="$returnvariable"/>));
                    </xsl:if>
                </xsl:when>


                <xsl:otherwise>
                    //Unknown style!! No code is generated
                    throw UnsupportedOperationException("Unknown Style");
                </xsl:otherwise>
            </xsl:choose>



            }
        </xsl:for-each>

        newMsgContext.setEnvelope(envelope);
        }



        } catch (Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        <xsl:for-each select="method"/>
        }

        <!-- generate the databind supporters-->
        //<xsl:apply-templates/>

        }
    </xsl:template>

     <!-- #################################################################################  -->
    <!-- ############################   xmlbeans template   ##############################  -->
    <xsl:template match="databinders[@dbtype='xmlbeans']">

        <xsl:variable name="base64"><xsl:value-of select="base64Elements/name"/></xsl:variable>
        <xsl:if test="$base64">
            private static javax.xml.namespace.QName[] qNameArray = {
            <xsl:for-each select="base64Elements/name">
                <xsl:if test="position()>1">,</xsl:if>new javax.xml.namespace.QName("<xsl:value-of select="@ns-url"/>","<xsl:value-of select="@localName"/>")
            </xsl:for-each>
            };
        </xsl:if>

        <xsl:for-each select="param">
            <xsl:if test="@type!=''">
                public  org.apache.axis2.om.OMElement  toOM(<xsl:value-of select="@type"/> param){
                org.apache.axis2.om.impl.llom.builder.StAXOMBuilder builder = new org.apache.axis2.om.impl.llom.builder.StAXOMBuilder
                (org.apache.axis2.om.OMAbstractFactory.getOMFactory(),new org.apache.axis2.util.StreamWrapper(param.newXMLStreamReader())) ;

                <xsl:choose>
                    <xsl:when test="$base64">
                         org.apache.axis2.om.OMElement documentElement = builder.getDocumentElement();
                         optimizeContent(documentElement,qNameArray);
                         return documentElement;
                    </xsl:when>
                    <xsl:otherwise>
                        return  builder.getDocumentElement();
                    </xsl:otherwise>
                </xsl:choose>

                }
            </xsl:if>

        </xsl:for-each>

        public org.apache.xmlbeans.XmlObject fromOM(org.apache.axis2.om.OMElement param,
        java.lang.Class type){
        try{
        <xsl:for-each select="param">
            <xsl:if test="@type!=''">
                if (<xsl:value-of select="@type"/>.class.equals(type)){
                return <xsl:value-of select="@type"/>.Factory.parse(param.getXMLStreamReader()) ;
                }
            </xsl:if>
        </xsl:for-each>
        }catch(java.lang.Exception e){
        throw new RuntimeException("Data binding error",e);
        }
        return null;
        }

    </xsl:template>

    <!-- #################################################################################  -->
    <!-- ############################   ADB template   ##############################  -->
    <xsl:template match="databinders[@dbtype='adb']">

         <xsl:variable name="base64"><xsl:value-of select="base64Elements/name"/></xsl:variable>
         <xsl:if test="$base64">
             private static javax.xml.namespace.QName[] qNameArray = {
             <xsl:for-each select="base64Elements/name">
                 <xsl:if test="position()>1">,</xsl:if>new javax.xml.namespace.QName("<xsl:value-of select="@ns-url"/>","<xsl:value-of select="@localName"/>")
             </xsl:for-each>
             };
         </xsl:if>

        <xsl:for-each select="param">
                    <xsl:if test="@type!=''">

                        public  org.apache.axis2.om.OMElement  toOM(<xsl:value-of select="@type"/> param){
                            if (param instanceof org.apache.axis2.databinding.ADBBean){
                                org.apache.axis2.om.impl.llom.builder.StAXOMBuilder builder = new org.apache.axis2.om.impl.llom.builder.StAXOMBuilder
                                (org.apache.axis2.om.OMAbstractFactory.getOMFactory(), param.getPullParser(null));
                                return builder.getDocumentElement();
                            }else{
                               <!-- treat this as a plain bean. use the reflective bean converter -->
                               <!-- todo finish this once the bean serializer has the necessary methods -->
                                retrun null;
                            }
                        }
                    </xsl:if>
                </xsl:for-each>


         public  java.lang.Object fromOM(org.apache.axis2.om.OMElement param,
         java.lang.Class type){
              Object obj;
             try {
                 java.lang.reflect.Method parseMethod = type.getMethod("parse",new Class[]{javax.xml.stream.XMLStreamReader.class});
                 obj = null;
                 if (parseMethod!=null){
                     obj = parseMethod.invoke(null,new Object[]{param.getXMLStreamReader()});
                 }else{
                     //oops! we don't know how to deal with this. Perhaps the reflective one is a good choice here
                    <!-- todo finish this once the bean serializer has the necessary methods -->
                 }
             } catch (Exception e) {
                  throw new RuntimeException(e);
             }

             return obj;
         }

     </xsl:template>
    <!-- #################################################################################  -->
    <!-- ############################   none template!!!   ##############################  -->
    <xsl:template match="databinders[@dbtype='none']">
        public  org.apache.axis2.om.OMElement fromOM(org.apache.axis2.om.OMElement param, java.lang.Class type){
           return param;
        }

        public  org.apache.axis2.om.OMElement  toOM(org.apache.axis2.om.OMElement param){
            return param;
        }
    </xsl:template>
</xsl:stylesheet>
