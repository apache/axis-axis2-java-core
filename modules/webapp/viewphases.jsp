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

/**
 * Author : Deepal Jayasinghe
 * Date: May 26, 2005
 * Time: 7:14:26 PM
 */
        %>
<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.engine.Phase,
                 java.util.ArrayList"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/adminheader.jsp"></jsp:include>
<h1>Available Phases</h1>
     <%
         ArrayList phases = (ArrayList)request.getSession().getAttribute(Constants.PHASE_LIST);
         request.getSession().setAttribute(Constants.PHASE_LIST,null);
         ArrayList tempList;
     %><h2><font color="blue">System Pre-defined Phases</font></h2>
     <b>InFlow Up to Dispatcher</b>
     <blockquote>
         <%
             tempList = (ArrayList)phases.get(0);
             for (int i = 0; i < tempList.size(); i++) {
                 Phase phase = (Phase) tempList.get(i);
         %><%=phase.getPhaseName()%><br><%
             }
         %>
         </blockquote>
         <b>InFaultFlow </b>
         <blockquote>
<%--         <%--%>
<%--             tempList = (ArrayList)phases.get(1);--%>
<%--             for (int i = 0; i < tempList.size(); i++) {--%>
<%--                 String phase = (String) tempList.get(i);--%>
<%--         %><%=phase%><br><%--%>
<%--             }--%>
<%--         %>--%>
         </blockquote>
         <b>OutFlow </b>
         <blockquote>
<%--         <%--%>
<%--             tempList = (ArrayList)phases.get(2);--%>
<%--             for (int i = 0; i < tempList.size(); i++) {--%>
<%--                 String phase = (String) tempList.get(i);--%>
<%--         %><%=phase%><br><%--%>
<%--             }--%>
<%--         %>--%>
         </blockquote>
         <b>OutFaultFlow </b>
         <blockquote>
<%--         <%--%>
<%--             tempList = (ArrayList)phases.get(3);--%>
<%--             for (int i = 0; i < tempList.size(); i++) {--%>
<%--                 String phase = (String) tempList.get(i);--%>
<%--         %><%=phase%><br><%--%>
<%--             }--%>
<%--         %>--%>
         </blockquote>
         <br>
         <h2><font color="blue">User Defined Phases</font></h2>
         <b>Inflow after Dispatcher</b>
         <blockquote>
         <%
             tempList = (ArrayList)phases.get(4);
             for (int i = 0; i < tempList.size(); i++) {
                 Phase phase = (Phase) tempList.get(i);
         %><%=phase.getPhaseName()%><br><%
             }
         %>
         </blockquote>
         <b>InFaultFlow after Dispatcher</b>
         <blockquote>
         <%
             tempList = (ArrayList)phases.get(5);
             for (int i = 0; i < tempList.size(); i++) {
                 Phase phase = (Phase) tempList.get(i);
         %><%=phase.getPhaseName()%><br><%
             }
         %>
         </blockquote>
         <b>OutFlow  </b>
         <blockquote>
         <%
             tempList = (ArrayList)phases.get(6);
             for (int i = 0; i < tempList.size(); i++) {
                 Phase phase = (Phase) tempList.get(i);
         %><%=phase.getPhaseName()%><br><%
             }
         %>
         </blockquote>
         <b>OutFaultFlow </b>
         <blockquote>
         <%
             tempList = (ArrayList)phases.get(7);
             for (int i = 0; i < tempList.size(); i++) {
                 Phase phase = (Phase) tempList.get(i);
         %><%=phase.getPhaseName()%><br><%
             }
         %>
         </blockquote>
<jsp:include page="include/adminfooter.jsp"></jsp:include>
