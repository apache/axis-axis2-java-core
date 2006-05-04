<%@ page import="org.apache.axis2.Constants" %>
<%@ page import="org.apache.axis2.description.AxisService" %>
<%@ page import="org.apache.axis2.description.AxisServiceGroup" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/adminheader.jsp"/>
<h1>Available Service Groups</h1>
<%
    Iterator axisServiceGroupIter = (Iterator) request.getSession().getAttribute(
            Constants.SERVICE_GROUP_MAP);
    request.getSession().setAttribute(Constants.SERVICE_GROUP_MAP,null);
    while (axisServiceGroupIter.hasNext()) {
        AxisServiceGroup axisServiceGroup = (AxisServiceGroup) axisServiceGroupIter.next();
        String groupName = axisServiceGroup.getServiceGroupName();
        ArrayList modules = axisServiceGroup.getEngagedModules();
        Iterator axisServiceIter = axisServiceGroup.getServices();
%>
<h2><%=groupName%></h2><ul>
    <%
        while (axisServiceIter.hasNext()){
            AxisService axisService = (AxisService) axisServiceIter.next();
            String serviceName = axisService.getName();
    %>
    <li><font color="blue"><a href="axis2-admin/ListSingleService?serviceName=<%=serviceName%>">
        <%=serviceName%></a></font></li>
    <%
        }
    %>
</ul>
<%
    if (modules.size() > 0) {
%>
<I>Engaged modules</I><ul>
    <%
        for (int i = 0; i < modules.size(); i++) {
            QName modulDesc = (QName) modules.get(i);
    %>
    <li><%=modulDesc.getLocalPart()%></li>
    <%
        }
    %></ul><%
        }
    }
%>
<jsp:include page="include/adminfooter.jsp"/>
