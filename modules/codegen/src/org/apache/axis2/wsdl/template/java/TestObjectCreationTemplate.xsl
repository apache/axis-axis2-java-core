<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <!-- #################################################################################  -->
    <!-- ############################   xmlbeans template   ##############################  -->
    <xsl:template match="databinders[@dbtype='xmlbeans']">
        //Create the desired XmlObject and provide it as the test object
        public  org.apache.xmlbeans.XmlObject getTestObject(java.lang.Class type) throws Exception{
        java.lang.reflect.Method creatorMethod = null;
                if (org.apache.xmlbeans.XmlObject.class.isAssignableFrom(type)){
                    Class[] declaredClasses = type.getDeclaredClasses();
                    for (int i = 0; i &lt; declaredClasses.length; i++) {
                        Class declaredClass = declaredClasses[i];
                        if (declaredClass.getName().endsWith("$Factory")){
                            creatorMethod = declaredClass.getMethod("newInstance",null);
                            break;
                        }

                    }
                }
                if (creatorMethod!=null){
                    return  (org.apache.xmlbeans.XmlObject)creatorMethod.invoke(null,null);
                }else{
                    throw new Exception("Creator not found!");
                }

        }

    </xsl:template>
    <!-- #################################################################################  -->
    <!-- ############################   jaxme template   ##############################  -->
    <xsl:template match="databinders[@dbtype='jaxme']">
        //Create the desired Object and provide it as the test object
        public  java.lang.Object getTestObject(java.lang.Class type) throws Exception{
            Class factoryClazz = org.apache.axis2.util.Loader.loadClass(type.getPackage().getName() + ".ObjectFactory"); 
            Object factory = factoryClazz.newInstance();   
            java.lang.reflect.Method creatorMethod = factoryClazz.getMethod("newInstance", new Class[]{ Class.class });
            if (creatorMethod != null) {
                return creatorMethod.invoke(factory, null);
            } else {
                throw new Exception("newInstance method not found!");
            }
        }

    </xsl:template>
    <!-- #################################################################################  -->
    <!-- ############################   ADB template   ###################################  -->
    <xsl:template match="databinders[@dbtype='adb']">
        //Create an ADBBean and provide it as the test object
        public org.apache.axis2.databinding.ADBBean getTestObject(java.lang.Class type) throws Exception{
           return (org.apache.axis2.databinding.ADBBean) type.newInstance();
        }

    </xsl:template>
    <!-- #################################################################################  -->
    <!-- ############################   none template!!!   ###############################  -->
    <xsl:template match="databinders[@dbtype='none']">
        //Create an OMElement and provide it as the test object
        public org.apache.axis2.om.OMElement getTestObject(java.lang.Object dummy){
           org.apache.axis2.om.OMFactory factory = org.apache.axis2.om.OMAbstractFactory.getOMFactory();
           org.apache.axis2.om.OMNamespace defNamespace = factory.createOMNamespace("",null);
           return org.apache.axis2.om.OMAbstractFactory.getOMFactory().createOMElement("test",defNamespace);
        }
    </xsl:template>

</xsl:stylesheet>