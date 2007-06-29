<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>


    <!-- #################################################################################  -->
    <!-- ############################   none template!!!   ##############################  -->
    <xsl:template match="databinders[@dbtype='none']">
        private  org.apache.axiom.om.OMElement fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault {
        return param;
        }

        private  org.apache.axiom.om.OMElement  toOM(org.apache.axiom.om.OMElement param, boolean optimizeContent) throws org.apache.axis2.AxisFault {
        return param;
        }

        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.apache.axiom.om.OMElement param, boolean optimizeContent)
        throws org.apache.axis2.AxisFault {
        org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(param);
        return envelope;
        }

        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory)
        throws org.apache.axis2.AxisFault {
        return factory.getDefaultEnvelope();
        }

    </xsl:template>

</xsl:stylesheet>