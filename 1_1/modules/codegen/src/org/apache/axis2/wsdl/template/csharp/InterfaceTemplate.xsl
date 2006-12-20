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
    <xsl:template match="/interface">
    <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
    <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>    
    namespace <xsl:value-of select="@package"/>
    {

    /// &lt;summary&gt;
    /// Auto generated C# interface by the Axis code generator
    /// This is meant to be used with the IKVM converted Axis libraries
    /// &lt;/summary&gt;

    public interface <xsl:value-of select="@name"></xsl:value-of>
    {

     <xsl:for-each select="method">
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>
         <xsl:variable name="inputtype"><xsl:value-of select="input/param/@type"></xsl:value-of></xsl:variable>  <!-- this needs to change-->
         <xsl:variable name="inputparam"><xsl:value-of select="input/param/@name"></xsl:value-of></xsl:variable>  <!-- this needs to change-->
         <xsl:if test="$isSync='1'">
        /// &lt;summary&gt;
        ///  Auto generated interface method
        /// &lt;/summary&gt;
         public  <xsl:if test="$outputtype=''">void</xsl:if><xsl:if test="$outputtype!=''"><xsl:value-of select="$outputtype"/></xsl:if><xsl:text> </xsl:text><xsl:value-of select="@name"/>(<xsl:if test="$inputtype!=''"><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="$inputparam"></xsl:value-of></xsl:if>);
         </xsl:if>

         <xsl:if test="$isAsync='1'">
        ///&lt;summary&gt;
        ///Auto generated interface method
        ///&lt;/summary&gt;
        ///<xsl:if test="$inputtype!=''">&lt;param  name="<xsl:value-of select="$inputparam"/>"/&gt;</xsl:if>
        ///
        public void start<xsl:value-of select="@name"/>(<xsl:if test="$inputtype!=''"><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="$inputparam"></xsl:value-of></xsl:if>) ;
        </xsl:if>

     </xsl:for-each>
    }
   }
    </xsl:template>
 </xsl:stylesheet>