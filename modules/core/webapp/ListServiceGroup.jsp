<%@ page import="org.apache.axis2.Constants" %>
<%@ page import="org.apache.axis2.description.ServiceDescription" %>
<%@ page import="org.apache.axis2.description.ModuleDescription" %>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.axis2.description.ServiceGroupDescription" %>
<%@ page import="javax.xml.namespace.QName"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>List Services Groups</title>
    <link href="css/axis-style.css" rel="stylesheet" type="text/css">
</head>

<body>
<h1>Available Service Groups</h1>
<%
    Iterator serviceGroup = (Iterator)request.getSession().getAttribute(
            Constants.SERVICE_GROUP_MAP);
    while(serviceGroup.hasNext()){
        ServiceGroupDescription groupDescription = (ServiceGroupDescription) serviceGroup.next();
        String groupName = groupDescription.getServiceGroupName();
        ArrayList modules = groupDescription.getServiceGroupModules();
        Iterator service = groupDescription.getServices();
%>
<h2><%=groupName%></h2><ul>
    <%
        while(service.hasNext()){
            ServiceDescription serviceDescription = (ServiceDescription) service.next();
            String serviceName = serviceDescription.getName().getLocalPart();
    %>
    <li><font color="blue"><a href="listGroupService.jsp?serviceName=<%=serviceName%>">
   <%=serviceName%> </a></font></li>
    <%
        }
    %>
</ul>
<%
    if(modules.size() >0){
%>
<I>Engaged modules</I><ul>
<%
     for (int i = 0; i < modules.size(); i++) {
        QName modulDesc = (QName)modules.get(i);
%>
 <li><%=modulDesc.getLocalPart()%></li>
<%
    }
        %></ul><%
    }

%>
<hr>
<%
    }
%>
</body>
</html>