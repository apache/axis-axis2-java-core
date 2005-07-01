<%@ page import="java.util.HashMap,
                 org.apache.axis2.Constants,
                 java.util.Collection,
                 java.util.Iterator,
                 org.apache.axis2.description.ModuleDescription,
                 org.apache.axis2.engine.AxisConfigurationImpl,
                 java.util.ArrayList,
                 org.apache.axis2.engine.Phase,
                 org.apache.axis2.engine.Handler"%>
 <%--
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

/**
 * Author : Deepal Jayasinghe
 * Date: May 26, 2005
 * Time: 7:14:26 PM
 */
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>View Global Handlers</title></head>
  <body>
   <%
            AxisConfigurationImpl axisConfig = (AxisConfigurationImpl)request.getSession().
                    getAttribute(Constants.GLOBAL_HANDLERS);
             if(axisConfig != null ){
                 ArrayList handlers ;
                 ArrayList phases = axisConfig.getInPhasesUptoAndIncludingPostDispatch();
                 %>
                 <h3> In Flow upto and including dispatcher </h3>
                 <hr>
                 <ul>
                 <%
                 for (int i = 0; i < phases.size(); i++) {
                     Phase phase = (Phase) phases.get(i);
                     %>
                     <li>Phase Name :  <%=phase.getPhaseName()%></li>
                     <ul>
                     <%
                      handlers = phase.getHandlers();
                      for (int j = 0; j < handlers.size(); j++) {
                          Handler handler = (Handler) handlers.get(j);
                          %>
                          <li>Handler Name : <%=handler.getHandlerDesc().getName().getLocalPart()%></li>
                          <%
                      }
                     %>
                     </ul>
                 <%
                 }
                 %>
                 </ul>
                 <%
                 phases = axisConfig.getInFaultFlow();
                 %>
                 <h3> In Fault Flow </h3>
                 <hr>
                 <ul>
                 <%
                 for (int i = 0; i < phases.size(); i++) {
                     Phase phase = (Phase) phases.get(i);
                     %>
                     <li>Phase Name  : <%=phase.getPhaseName()%></li>
                     <ul>
                     <%
                      handlers = phase.getHandlers();
                      for (int j = 0; j < handlers.size(); j++) {
                          Handler handler = (Handler) handlers.get(j);
                          %>
                          <li>Handler Name : <%=handler.getHandlerDesc().getName().getLocalPart()%></li>
                          <%
                      }
                     %>
                     </ul>
                 <%
                 }
                 %>
                 </ul>
                 <%

                 phases = axisConfig.getOutFlow();
                 %>
                 <h3> Out Flow </h3>
                 <hr>
                 <ul>
                 <%
                 for (int i = 0; i < phases.size(); i++) {
                     Phase phase = (Phase) phases.get(i);
                     %>
                     <li>Phase Name : <%=phase.getPhaseName()%></li>
                     <ul>
                     <%
                      handlers = phase.getHandlers();
                      for (int j = 0; j < handlers.size(); j++) {
                          Handler handler = (Handler) handlers.get(j);
                          %>
                          <li>Handler Name : <%=handler.getHandlerDesc().getName().getLocalPart()%></li>
                          <%
                      }
                     %>
                     </ul>
                 <%
                 }
                 %>
                 </ul>
                 <%
                 phases = axisConfig.getOutFaultFlow();
                 %>
                 <h3> Out Fault Flow </h3>
                 <hr>
                 <ul>
                 <%
                 for (int i = 0; i < phases.size(); i++) {
                     Phase phase = (Phase) phases.get(i);
                     %>
                     <li>Phase Name : <%=phase.getPhaseName()%></li>
                     <ul>
                     <%
                      handlers = phase.getHandlers();
                      for (int j = 0; j < handlers.size(); j++) {
                          Handler handler = (Handler) handlers.get(j);
                          %>
                          <li>Handler Name : <%=handler.getHandlerDesc().getName().getLocalPart()%></li>
                          <%
                      }
                     %>
                     </ul>
                 <%
                 }
                 %>
                 </ul>
                 <%

             }
              %>

  </body>
</html>