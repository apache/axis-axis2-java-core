<%@ page import="org.apache.axis.Constants,
                 org.apache.axis.description.ServiceDescription,
                 org.apache.axis.description.OperationDescription,
                 java.util.*,
                 javax.xml.namespace.QName,
                 org.apache.axis.description.ModuleDescription"%>
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
