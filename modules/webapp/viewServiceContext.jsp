<%@ page import="org.apache.axis2.Constants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.axis2.context.ServiceContext" %>
<%@ page import="org.apache.axis2.context.ServiceGroupContext" %>
<%@ page import="org.apache.axis2.description.AxisService" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.apache.axis2.context.ContextFactory" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
%>
<jsp:include page="include/adminheader.jsp"/>
<h1>Running Context Hierarchy</h1>
<%
    ConfigurationContext configContext = (ConfigurationContext) request.getSession().getAttribute(Constants.CONFIG_CONTEXT);
    Hashtable serviceGroupContextsMap = configContext.getServiceGroupContexts();
    String type = request.getParameter("TYPE");
    String sgID = request.getParameter("PID");
    String ID = request.getParameter("ID");
    ServiceGroupContext sgContext = (ServiceGroupContext) serviceGroupContextsMap.get(sgID);
    AxisService service = sgContext.getDescription().getService(ID);
    ServiceContext serviceContext = ContextFactory.createServiceContext(sgContext,service);
    if (sgID != null && serviceContext != null) {
        if (type != null) {
            if ("VIEW".equals(type)) {
                Map perMap = serviceContext.getProperties();
                if (perMap.size() > 0) {
%>
<h4>Persistance Properties</h4><ul>
    <%
        Iterator itr = perMap.keySet().iterator();
        while (itr.hasNext()) {
            String key = (String) itr.next();
            Object property = perMap.get(key);
    %>
    <li><%=key%> : <%=property.toString()%></li>
    <%
        }
    %></ul>
<%
} else {
%>
<h4>No persistance properties found in the context</h4>
<%
        }
    }
} else {
%> <h4>No Service Context Found</h4><%
        }
    }
%>
<jsp:include page="include/adminfooter.inc"/>