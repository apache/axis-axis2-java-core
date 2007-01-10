<!--
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/bean">
    namespace <xsl:value-of select="@package"/>
    {

    ///  &lt;summary&gt;
    ///  Auto generated bean class by the Axis code generator
    ///  This is meant to be used with the IKVM converted Axis libraries
    /// &lt;/summary&gt;

    public class <xsl:value-of select="@name"/> {


     <xsl:for-each select="property">
         <xsl:variable name="propertyType"><xsl:value-of select="@type"></xsl:value-of></xsl:variable>
         <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>

        /// &lt;summary&gt;
        /// field for <xsl:value-of select="$propertyName"/>
        ///&lt;/summary&gt;
         private <xsl:value-of select="$propertyType"/> local<xsl:value-of select="$propertyName"/>;

        /// &lt;summary&gt;
        /// Auto generated getter method
        ///&lt;/summary&gt;
        /// <returns><xsl:value-of select="$propertyType"/></returns>
        ///
        public  <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text>get<xsl:value-of select="$propertyName"/>(){
             return local<xsl:value-of select="$propertyName"/>;
        }

        /// &lt;summary&gt;
        /// Auto generated setter method
        ///&lt;/summary&gt;
        ///<param name="param{$propertyName}"> </param>
        ///
        public void set<xsl:value-of select="$propertyName"/>(<xsl:value-of select="$propertyType"/> param<xsl:value-of select="$propertyName"/>){
             this.local<xsl:value-of select="$propertyName"/>=param<xsl:value-of select="$propertyName"/>;
        }
     </xsl:for-each>
    }
    }
    </xsl:template>
 </xsl:stylesheet>