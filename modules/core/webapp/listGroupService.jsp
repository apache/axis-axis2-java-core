<%@ page import="org.apache.axis2.Constants"%>
<%@ page import="org.apache.axis2.description.ServiceDescription"%>
<%@ page import="org.apache.axis2.description.ModuleDescription"%>
<%@ page import="org.apache.axis2.description.OperationDescription"%>
<%@ page import="java.util.*"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>List Services</title>
    <link href="css/axis-style.css" rel="stylesheet" type="text/css">
</head>
<body>
<h1>Available services</h1>
<%String IP=request.getRequestURL().toString();
        int lastindex = IP.lastIndexOf('/');
        IP = IP.substring(0,lastindex);
        String prifix = IP + "/services/";
    %>
<%
         HashMap serviceMap = (HashMap)request.getSession().getAttribute(Constants.SERVICE_MAP);
          String servicName = request.getParameter("serviceName");
          ServiceDescription axisService = (ServiceDescription)serviceMap.get(servicName);
         if (axisService!=null){
             HashMap operations;
             String serviceName = "";
             Collection operationsList;
                 operations = axisService.getOperations();
                 operationsList = operations.values();
                 serviceName = axisService.getName().getLocalPart();
     %><hr><h2><font color="blue"><a href="<%=prifix + axisService.getName().getLocalPart()%>?wsdl"><%=serviceName%></a></font></h2>
<font color="blue">Service EPR : <font color="black"><%=prifix + axisService.getName().getLocalPart()%></font>
    <h4>Service Description : <font color="black"><%=axisService.getServiceDescription()%></h4>
    <%
        Collection engagdeModules = axisService.getEngagedModules();
        String moduleName = "";
        if(engagdeModules.size() >0){
    %>
    <i>Engaged Modules for the service</i>
    <%
        for (Iterator iteratorm = engagdeModules.iterator(); iteratorm.hasNext();) {
            ModuleDescription description = (ModuleDescription) iteratorm.next();
            moduleName = description.getName().getLocalPart();
    %><li><%=moduleName%></li>
    <br>
    <%
            }
        }
        if (operationsList.size() > 0) {
    %><br><i>Available operations</i><%
} else {
%><i> There are no any opeartions specified</i><%
    }
%><ul><%
    for (Iterator iterator1 = operationsList.iterator(); iterator1.hasNext();) {
        OperationDescription axisOperation = (OperationDescription) iterator1.next();
%><li><%=axisOperation.getName().getLocalPart()%></li>
    <%--                 <br>Opeartion EPR : <%=prifix + axisService.getName().getLocalPart() + "/"+ axisOperation.getName().getLocalPart()%>--%>
    <%
        engagdeModules = null;
        engagdeModules = axisOperation.getModules();
        moduleName = "";
        if(engagdeModules.size() >0){
    %>
    <br><i>Engaged Modules for the opeartion</i><ul>
    <%
        for (Iterator iterator2 = engagdeModules.iterator(); iterator2.hasNext();) {
            ModuleDescription description = (ModuleDescription) iterator2.next();
            moduleName = description.getName().getLocalPart();
    %><li><%=moduleName%></li><br><%
    }
%></ul><%
        }

    }
%></ul>
    <%
        }
    %>
</body>
</html>