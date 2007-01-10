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
    <xsl:template match="/callback">
    namespace <xsl:value-of select="@package"/>
    {
    ///&lt;summary&gt;
    /// Auto generated C# Callback class by the Axis code generator
    /// This is meant to be used with the IKVM converted Axis libraries
    /// &lt;/summary&gt;

    public class <xsl:value-of select="@name"/>
    {


	<xsl:for-each select="method">

        /// &lt;summary&gt;
        /// Auto generated Axis2 call back method
        ///&lt;/summary&gt;
        ///
        public void receiveResult<xsl:value-of select="@name"/>(org.apache.axis2.client.async.AsyncResult result)
        {
			//Fill here with the code to handle the response
			
        }

         ///&lt;summary&gt;
         /// Auto generated Axis2 Error handler
         /// &lt;/summary&gt;
         ///
        public void receiveError<xsl:value-of select="@name"/>(java.lang.Exception e)
        {
			//Fill here with the code to handle the exception

        }
     </xsl:for-each>

     
    }
  }
    </xsl:template>
 </xsl:stylesheet>