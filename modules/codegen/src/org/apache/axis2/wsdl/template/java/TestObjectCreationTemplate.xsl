<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <!-- #################################################################################  -->
    <!-- ############################   xmlbeans template   ##############################  -->
    <xsl:template match="databinders[@dbtype='xmlbeans']">
        //Create an ADBBean and provide it as the test object
        //todo Fill this properly
        public  org.apache.xmlbeans.XmlObject getTestObject(java.lang.Class type) throws Exception{
           <!--Need to fix this-->
           return null;
        }

    </xsl:template>
    <!-- #################################################################################  -->
    <!-- ############################   ADB template   ###################################  -->
    <xsl:template match="databinders[@dbtype='adb']">
        //Create an ADBBean and provide it as the test object
        //todo Fill this properly
        public org.apache.axis2.databinding.ADBBean getTestObject(java.lang.Class type) throws Exception{
           org.apache.axis2.databinding.ADBBean bean = (org.apache.axis2.databinding.ADBBean)type.newInstance();


           return bean;
        }

    </xsl:template>
    <!-- #################################################################################  -->
    <!-- ############################   none template!!!   ###############################  -->
    <xsl:template match="databinders[@dbtype='none']">
        //Create an OMElement and provide it as the test object
        //todo Fill this properly
        public org.apache.axis2.om.OMElement getTestObject(java.lang.Object dummy){

           org.apache.axis2.om.OMFactory factory = org.apache.axis2.om.OMAbstractFactory.getOMFactory();
           org.apache.axis2.om.OMNamespace defNamespace = factory.createOMNamespace("",null);
           return org.apache.axis2.om.OMAbstractFactory.getOMFactory().createOMElement("test",defNamespace);


        }
    </xsl:template>

</xsl:stylesheet>