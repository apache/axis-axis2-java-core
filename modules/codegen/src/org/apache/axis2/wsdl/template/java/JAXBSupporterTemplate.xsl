<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
    /**
     * <xsl:value-of select="@name"/>.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: #axisVersion# #today#
     */
    package <xsl:value-of select="@package"/>;
    import java.io.FileOutputStream;
    import java.io.FileInputStream;
    
    import javax.xml.bind.JAXBContext;
    import javax.xml.bind.Marshaller;
    import javax.xml.bind.Unmarshaller;
    import javax.xml.stream.*;
    
    /**
     *  <xsl:value-of select="@name"/> supporter class for JAXB data binding
     */

    public class <xsl:value-of select="@name"/> {
             <xsl:apply-templates />

          public static java.lang.Object fromOM(org.apache.axis2.om.OMElement param,
               java.lang.Class type){
                try{
                	FileOutputStream fos = new FileOutputStream("_temp_");
	                XMLStreamWriter writer = XMLOutputFactory.newInstance()
			     	.createXMLStreamWriter(new FileOutputStream("_temp_"));
			param.serialize(writer);
	                writer.flush();
	                fos.flush();
	                fos.close();
	                JAXBContext jaxbContext = JAXBContext.newInstance("org.simpletest.xsd");
	                		//Above org.simpletest.xsd is being hardcoded, but it should actually be the 
	                		//package corresponding to targetnamespace of schema.
	                Unmarshaller unmarsh = jaxbContext.createUnmarshaller();
	                FileInputStream xmlContent = new FileInputStream("_temp_");

                    <xsl:for-each select="param">
                     <xsl:if test="@type!=''">
                     if (<xsl:value-of select="@type"/>.class.equals(type)){
                    	 <xsl:value-of select="@type"/> unmarshalledClazz = (<xsl:value-of select="@type"/>)unmarsh.unmarshal(xmlContent);
                         return unmarshalledClazz;
                     }
                     </xsl:if>
                    </xsl:for-each>
                 }catch(java.lang.Exception e){
                    throw new RuntimeException("Data binding error",e);
                }
             return null;
          }

        //Generates an empty object for testing
        // Caution - need some manual editing to work properly
        
         public static java.lang.Object getTestObject(java.lang.Class type){
             //Returning empty object for compilation to succeed. Don't rely on
             //using the test object out of this method, as it is.
             try {
             <xsl:for-each select="param">
	        <xsl:if test="@type!=''">
                    if (<xsl:value-of select="@type"/>.class.equals(type)){
                    <xsl:value-of select="@type"/> emptyObject= <xsl:value-of select="@type"/>.class.newInstance();
                    return emptyObject;
                    }
                </xsl:if>
             </xsl:for-each>
             return null;
             } catch(java.lang.Exception e){
                 throw new RuntimeException("Test object creation failure",e);
             }
          }
          
     }
    </xsl:template>
    
    <xsl:template match="param">
        <xsl:if test="@type!=''">
          public  static org.apache.axis2.om.OMElement  toOM(<xsl:value-of select="@type"/> param){
          try {
          	JAXBContext jaxbContext = JAXBContext.newInstance("org.simpletest.xsd");
          		//Above org.simpletest.xsd is being hardcoded, but it should actually be the 
	              	//package corresponding to targetnamespace of schema.
        	Marshaller marsh = jaxbContext.createMarshaller();
        		//we want the output to be neatly indented, right?
        	marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
        			   new Boolean(true));
        	FileOutputStream tempFOP = new FileOutputStream("_temp_");
        	System.out.println("JAXB marshalled output of the java instance:");
        	marsh.marshal(param,System.out);
        	marsh.marshal(param, tempFOP);
 
        	tempFOP.flush();
        	tempFOP.close();
        	XMLStreamReader reader = XMLInputFactory.newInstance().
     			createXMLStreamReader(new FileInputStream("_temp_"));
		org.apache.axis2.om.impl.llom.builder.StAXOMBuilder builder = new org.apache.axis2.om.impl.llom.builder.StAXOMBuilder
			(org.apache.axis2.om.OMAbstractFactory.getOMFactory(),new org.apache.axis2.util.StreamWrapper(reader)) ;
		org.apache.axis2.om.OMElement documentElement = builder.getDocumentElement();
		//Building the element is needed to avoid certain stream errors!
		documentElement.build();
		return documentElement;
	  } catch(Exception e) {
        	System.err.println("Exception encountered in echoStringDatabindingSupporter.toOM(). Returning null");
      	  }
        	return null;
         }
       </xsl:if>
    </xsl:template>
 </xsl:stylesheet>