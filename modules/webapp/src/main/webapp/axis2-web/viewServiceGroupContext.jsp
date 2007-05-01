<%@ page import="org.apache.axis2.Constants"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.apache.axis2.context.ServiceGroupContext"%>
<%@ page import="java.util.Hashtable"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.Map"%>
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
<jsp:include page="include/adminheader.jsp"></jsp:include>
<h1>Runing Context hierachy</h1>
<%
    ConfigurationContext configContext = (ConfigurationContext)request.getSession().getAttribute(Constants.CONFIG_CONTEXT);
    Hashtable serviceGroupContextsMap = configContext.getServiceGroupContexts();
    String type = request.getParameter("TYPE");
    String sgID = request.getParameter("ID");
    ServiceGroupContext sgContext = (ServiceGroupContext)serviceGroupContextsMap.get(sgID);
    if(sgID !=null && sgContext !=null){
        if(type != null){
            if("VIEW".equals(type)){
             Map perMap = sgContext.getProperties();
             if(perMap.size()>0){
             %>
             <h4>Persistance properties</h4><ul>
             <%
                 Iterator itr = perMap.keySet().iterator();
                 while (itr.hasNext()) {
                     String key = (String) itr.next();
                     Object property =  perMap.get(key);
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
            }   else if("DELETE".equals(type)){
                Object obj = serviceGroupContextsMap.remove(sgID);
                if(obj != null){
                 %>Removed the context<%
            }else {
                %>Unable to remove the context <%
            }
            }
        }
    } else {
%> <h4>No service group context found</h4><%
    }
%>
<jsp:include page="include/adminfooter.inc"></jsp:include>