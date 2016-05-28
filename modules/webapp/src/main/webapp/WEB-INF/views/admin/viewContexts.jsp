<%--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.apache.axis2.Constants"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.apache.axis2.context.ServiceContext"%>
<%@ page import="org.apache.axis2.context.ServiceGroupContext"%>
<%@ page import="java.util.Iterator"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/include/adminheader.jsp"/>
<h1>Running Context Hierarchy</h1>
<%
    ConfigurationContext configContext = (ConfigurationContext) request.getSession().getAttribute(
            Constants.CONFIG_CONTEXT);

    String[] serviceGroupIds = configContext.getServiceGroupContextIDs();
    if (serviceGroupIds.length > 0) {
%>
<ul>
    <%

   for (int i = 0; i < serviceGroupIds.length; i++) {
        String groupContextID = serviceGroupIds[i];
        ServiceGroupContext groupContext = configContext.getServiceGroupContext(groupContextID);
        %>
           <li><%=groupContextID%><a style="color:blue" href="<c:url value="axis2-admin/viewServiceGroupContext"><c:param name="TYPE" value="VIEW"/><c:param name="ID" value="<%=groupContextID%>"/></c:url>">
                    View</a>  <a style="color:red" href="<c:url value="axis2-admin/viewServiceGroupContext"><c:param name="TYPE" value="DELETE"/><c:param name="ID" value="<%=groupContextID%>"/></c:url>">
                    Remove</a> </li>
                    
                    <li>
        <%
        Iterator serviceContextItr = groupContext.getServiceContexts();
            %><ul><%
        while (serviceContextItr.hasNext()) {
            ServiceContext serviceContext = (ServiceContext)serviceContextItr.next();
             String serviceConID = serviceContext.getAxisService().getName();
        %>
            <li><%=serviceConID%><a style="color:blue" href="<c:url value="axis2-admin/viewServiceContext"><c:param name="TYPE" value="VIEW"/><c:param name="ID" value="<%=serviceConID%>"/><c:param name="PID" value="<%=groupContextID%>"/></c:url>">
                    View</a></li>
        <%
        }
                %></ul><hr>
                </li><%
    }
    %>  </ul>
        <%
            } else {%>
	<p>No running contexts were found on the system.</p>
            <%}
%>
<jsp:include page="/WEB-INF/include/adminfooter.jsp"/>
