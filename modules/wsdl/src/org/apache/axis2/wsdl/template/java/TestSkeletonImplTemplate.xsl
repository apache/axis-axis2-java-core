<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/class">
	<xsl:variable name="namespace"><xsl:value-of select="@namespace"/></xsl:variable>
    package <xsl:value-of select="@package"/>;
    
		import javax.xml.namespace.QName;
		
		import org.apache.axis2.om.OMAbstractFactory;
		import org.apache.axis2.om.OMElement;
		import org.apache.axis2.om.OMFactory;
		import org.apache.axis2.om.impl.llom.OMTextImpl;
		

    /**
     *  Auto generated java skeleton for the service by the Axis code generator
     */

    public class <xsl:value-of select="@name"></xsl:value-of> extends <xsl:value-of select="@implpackage"/>.<xsl:value-of select="@servicename"></xsl:value-of>{
     <xsl:for-each select="method">
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>

         <xsl:variable name="inputtype"><xsl:value-of select="input/param/@type"></xsl:value-of></xsl:variable>  <!-- this needs to change-->
         <xsl:variable name="inputparam"><xsl:value-of select="input/param/@name"></xsl:value-of></xsl:variable>  <!-- this needs to change-->

        /**
         * Auto generated method signature
         *<xsl:if test="$inputtype!=''">@param <xsl:value-of select="$inputparam"></xsl:value-of></xsl:if>
         */
        public  <xsl:if test="$outputtype=''">void</xsl:if><xsl:if test="$outputtype!=''"><xsl:value-of select="$outputtype"/></xsl:if><xsl:text> </xsl:text><xsl:value-of select="@name"/>(<xsl:if test="$inputtype!=''"><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="$inputparam"></xsl:value-of></xsl:if>){
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