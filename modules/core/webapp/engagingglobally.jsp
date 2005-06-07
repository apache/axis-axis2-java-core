 <%
           /*
* Copyright 2004,2005 The Apache Software Foundation.
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
*
*
*/

/**
 * Author : Deepal Jayasinghe
 * Date: May 26, 2005
 * Time: 7:14:26 PM
 */
        %>

<%@ page import="java.util.Collection,
                 org.apache.axis.Constants,
                 java.util.Iterator,
                 javax.xml.namespace.QName,
                 org.apache.axis.description.ModuleDescription,
                 java.util.HashMap,
                 java.util.Enumeration"%><html>
<head>
<title>Engaging Module Globally</title>
<style type="text/css">
</style></head>

<body>
<%
    String status = (String)request.getSession().getAttribute(Constants.ENGAGE_STATUS);
%>



<form method="get" name="engaginModule" action="engagingglobally">
<table border="0" width="100%" cellspacing="1" cellpadding="1">
<tr>
<td>
&nbsp;
&nbsp;
</td>
</tr>
<tr>
<td>Select a Module :</td>
</tr><tr>
<td>
<select name="modules">
            <%
                HashMap moduels = (HashMap)request.getSession().getAttribute(Constants.MODULE_MAP);
                Collection moduleCol =  moduels.values();
                for (Iterator iterator = moduleCol.iterator(); iterator.hasNext();) {
                    ModuleDescription description = (ModuleDescription) iterator.next();
                    String modulename = description.getName().getLocalPart();
            %> <option  align="left" value="<%=modulename%>"><%=modulename%></option>
             <%
                }
             %>
             </td>
             </tr>
             <tr>
             <td>
             &nbsp;
             &nbsp;
             </td>
             </tr>
             <tr>
             <td>
             <input name="submit" type="submit" value=" Engage " >
             </td>
             </tr>

               <tr>
             <td>
             &nbsp;
             &nbsp;
             </td>
             </tr>
               <tr>
             <td>
             &nbsp;
             &nbsp;
             </td>
             </tr>
             <td>
              <textarea cols="50"  <%
              if(status == null){
                  %>
                  style="display:none"
                  <%
              } %>
              ><%=status%></textarea>
              </td>
             </tr>
             </table>
             </form>
             </body>
             </html>


