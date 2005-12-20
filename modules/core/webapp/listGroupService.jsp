<%@ page import="org.apache.axis2.Constants" %>
<%@ page import="org.apache.axis2.description.AxisOperation" %>
<%@ page import="org.apache.axis2.description.AxisService" %>
<%@ page import="org.apache.axis2.description.ModuleDescription" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/adminheader.jsp">
</jsp:include>
<h1>Available services</h1>
<%String IP = request.getRequestURL().toString();
    int lastindex = IP.lastIndexOf('/');
    IP = IP.substring(0, lastindex);
    String prifix = IP + "/services/";
%>
<%
    HashMap serviceMap = (HashMap) request.getSession().getAttribute(Constants.SERVICE_MAP);
    String servicName = request.getParameter("serviceName");
    AxisService axisService = (AxisService) serviceMap.get(servicName);
    if (axisService != null) {
        HashMap operations;
        String serviceName ;
        Collection operationsList;
        operations = axisService.getOperations();
        operationsList = operations.values();
        serviceName = axisService.getName();
%><hr>

<h2><font color="blue"><a href="<%=prifix + axisService.getName()%>?wsdl"><%=serviceName%></a>
</font></h2>
<font color="blue">Service EPR : <font color="black"><%=prifix + axisService.getName()%></font>
    <h4>Service Description : <font color="black"><%=axisService.getServiceDescription()%></h4>
    <%
        Collection engagdeModules = axisService.getEngagedModules();
        String moduleName ;
        if(engagdeModules.size() >0){
    %>
    <i>Engaged Modules for the axisService</i><ul>
    <%
        for (Iterator iteratorm = engagdeModules.iterator(); iteratorm.hasNext();) {
            ModuleDescription axisOperation = (ModuleDescription) iteratorm.next();
            moduleName = axisOperation.getName().getLocalPart();
    %><li><%=moduleName%></li>
    <%
        }%>
</ul>
    <%}
        if (operationsList.size() > 0) {
    %><br><i>Available operations</i><%
} else {
%><i> There are no operations specified</i><%
    }
%><ul><%
    for (Iterator iterator1 = operationsList.iterator(); iterator1.hasNext();) {
        AxisOperation axisOperation = (AxisOperation) iterator1.next();
%><li><%=axisOperation.getName().getLocalPart()%></li>
    <%--                 <br>Operation EPR : <%=prifix + axisService.getName().getLocalPart() + "/"+ axisOperation.getName().getLocalPart()%>--%>
    <%
        engagdeModules = axisOperation.getEngagedModules();
        if (engagdeModules.size() > 0) {
    %>
    <br><i>Engaged Modules for the Operation</i><ul>
    <%
        for (Iterator iterator2 = engagdeModules.iterator(); iterator2.hasNext();) {
            ModuleDescription moduleDecription = (ModuleDescription) iterator2.next();
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
    <jsp:include page="include/adminfooter.jsp"></jsp:include>
