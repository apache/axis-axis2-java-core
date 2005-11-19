<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
	<xsl:variable name="namespace"><xsl:value-of select="@namespace"/></xsl:variable>
    /**
     * <xsl:value-of select="@name"/>.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: #axisVersion# #today#
     */
    package <xsl:value-of select="@package"/>;
    
		import javax.xml.namespace.QName;
		
		import org.apache.axis2.om.OMAbstractFactory;
		import org.apache.axis2.om.OMElement;
		import org.apache.axis2.om.OMFactory;
		import org.apache.axis2.om.impl.llom.OMTextImpl;
		

    /**
     *  <xsl:value-of select="@name"/> Skeleton for the Axis Service
     */

    public class <xsl:value-of select="@name"></xsl:value-of> extends <xsl:value-of select="@implpackage"/>.<xsl:value-of select="@servicename"></xsl:value-of>{
     <xsl:for-each select="method">
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>

        /**
         * Auto generated method signature
         <xsl:for-each select="input/param">
         * @param <xsl:value-of select="@name"/>
         </xsl:for-each>
         */
        public  <xsl:if test="$outputtype=''">void</xsl:if><xsl:if test="$outputtype!=''"><xsl:value-of select="$outputtype"/></xsl:if><xsl:text> </xsl:text><xsl:value-of select="@name"/>(<xsl:for-each select="input/param"><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"></xsl:value-of><xsl:if test="not(position()=last())">, </xsl:if></xsl:for-each>){
                //Todo fill this with the necessary business logic
                <xsl:if test="$outputtype!=''"> 
                //Returns an simple om element
        	OMFactory factory = OMAbstractFactory.getOMFactory();
			OMElement element = factory.createOMElement(new QName("<xsl:value-of select="$namespace"/>", "<xsl:value-of select="generate-id()"/>"), null);
			OMElement element1 = factory.createOMElement(new QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="generate-id()"/>"), element);
			element.addChild(element1);
        	OMTextImpl text = new OMTextImpl("<xsl:value-of select="generate-id()"/>");
        	element1.addChild(text);
        	return element;
        	</xsl:if>
        }


     </xsl:for-each>
    }
    </xsl:template>
 </xsl:stylesheet>