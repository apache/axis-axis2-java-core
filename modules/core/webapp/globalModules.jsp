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
<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.ServiceDescription,
                 org.apache.axis2.description.OperationDescription,
                 java.util.*,
                 javax.xml.namespace.QName,
                 org.apache.axis2.description.ModuleDescription"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>Globally Engaged Modules </title>
<link href="css/axis-style.css" rel="stylesheet" type="text/css">
</head>
<body>
<h1>Globally Engaged Modules</h1>
     <%
         boolean foundModules = false;
         String modulename = "";
         Collection moduleCol = (Collection)request.getSession().getAttribute(Constants.MODULE_MAP);
         if(moduleCol != null && moduleCol.size() > 0) {
             for (Iterator iterator = moduleCol.iterator(); iterator.hasNext();) {
                 QName description = (QName) iterator.next();
                 modulename = description.getLocalPart();
     %><hr><h2><font color="blue"><%=modulename%></font></h2>
     <br> <%
             }
         } else{
     %>
     <h2><font color="blue">There is no module engaged globally</font></h2>
                 <%
         }
                 %>
                 </body>
</html>
