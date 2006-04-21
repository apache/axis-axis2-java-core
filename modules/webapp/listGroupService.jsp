<%@ page import="org.apache.axis2.Constants" %>
<%@ page import="org.apache.axis2.description.AxisModule" %>
<%@ page import="org.apache.axis2.description.AxisOperation" %>
<%@ page import="org.apache.axis2.description.AxisService" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <jsp:include page="include/httpbase.jsp"/>
  <title>List Services</title>
  <link href="axis2-web/css/axis-style.css" rel="stylesheet" type="text/css" />
</head>

<body>
<jsp:include page="include/adminheader.jsp">
</jsp:include>
<h1>Available services</h1>
<%
  String prifix = request.getAttribute("frontendHostUrl") + "services/";
%>
<%
  HashMap serviceMap = (HashMap) request.getSession().getAttribute(Constants.SERVICE_MAP);
  String servicName = request.getParameter("serviceName");
  AxisService axisService = (AxisService) serviceMap.get(servicName);
  if (axisService != null) {
    Iterator operations;
    String serviceName;
    operations = axisService.getOperations();
    serviceName = axisService.getName();
%><hr>

<h2><font color="blue"><a href="<%=prifix + axisService.getName()%>?wsdl"><%=serviceName%></a>
</font></h2>
<font color="blue">Service EPR :</font><font color="black"><%=prifix + axisService.getName()%></font>
<h4>Service Description : <font color="black"><%=axisService.getServiceDescription()%></h4>
<i><font color="blue">Service Status : <%=axisService.isActive() ? "Active" : "InActive"%></font></i><br/>
<%
  Collection engagedModules = axisService.getEngagedModules();
  String moduleName;
  if (engagedModules.size() > 0) {
%>
<i>Engaged Modules for the axisService</i><ul>
  <%
    for (Iterator iteratorm = engagedModules.iterator(); iteratorm.hasNext();) {
      AxisModule axisOperation = (AxisModule) iteratorm.next();
      moduleName = axisOperation.getName().getLocalPart();
  %><li><%=moduleName%></li>
  <%
    }%>
</ul>
<%
  }
  if (operations.hasNext()) {
%><br><i>Available operations</i><%
} else {
%><i> There are no operations specified</i><%
  }
%><ul><%
  operations = axisService.getOperations();
  while (operations.hasNext()) {
    AxisOperation axisOperation = (AxisOperation) operations.next();
%><li><%=axisOperation.getName().getLocalPart()%></li>
  <%
    engagedModules = axisOperation.getEngagedModules();
    if (engagedModules.size() > 0) {
  %>
  <br><i>Engaged Modules for the Operation</i><ul>
  <%
    for (Iterator iterator2 = engagedModules.iterator(); iterator2.hasNext();) {
      AxisModule moduleDecription = (AxisModule) iterator2.next();
      moduleName = moduleDecription.getName().getLocalPart();
  %><li><%=moduleName%></li><br><%
  }
%></ul><%
    }

  }
%></ul>
<%
  }
%>
<jsp:include page="include/adminfooter.jsp">
</jsp:include>
</body>
</html>
