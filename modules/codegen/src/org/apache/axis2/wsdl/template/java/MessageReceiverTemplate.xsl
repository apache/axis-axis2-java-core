<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <xsl:include href="databindsupporter"/>

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
                                <xsl:if test="@type!=''">(<xsl:value-of select="@type"/>)fromOM(msgContext.getEnvelope().getBody().getFirstElement(), <xsl:value-of select="@type"/>.class)<xsl:if test="position() &gt; 1">,</xsl:if></xsl:if>
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


</xsl:stylesheet>
