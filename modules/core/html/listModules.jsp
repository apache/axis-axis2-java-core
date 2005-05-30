<%@ page import="org.apache.axis.Constants,
                 org.apache.axis.description.ServiceDescription,
                 org.apache.axis.description.OperationDescription,
                 java.util.*,
                 javax.xml.namespace.QName,
                 org.apache.axis.description.ModuleDescription"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>List Available Moules</title>
  <link href="css/axis-style.css" rel="stylesheet" type="text/css">
  </head>
  <body>
  <h1>Available Modules</h1>
     <%
         HashMap moduleMap = (HashMap)request.getSession().getAttribute(Constants.MODULE_MAP);
         if (moduleMap!=null && !moduleMap.isEmpty()){
             String modulename = "";
             Collection moduleNames = moduleMap.values();
             boolean foundModules = false;
             for (Iterator iterator = moduleNames.iterator(); iterator.hasNext();) {
                 foundModules = true;
                 ModuleDescription  moduleQName = (ModuleDescription) iterator.next();
                 modulename = moduleQName.getName().getLocalPart();
     %><hr><h2><font color="blue"><%=modulename%></font></h2>
     <br> <%
             }
             if(! foundModules) {
                 %>
                 <h2><font color="blue">There is no module deployed in the system</font></h2>
                 <%
             }
         }
     %>
  </body>
</html>
