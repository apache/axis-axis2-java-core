<%--
author : Deepal Jayasinghe
--%>
<%@ page import="org.apache.axis.Constants,
                 org.apache.axis.description.ServiceDescription,
                 org.apache.axis.description.ModuleDescription,
                 org.apache.axis.description.OperationDescription,
                 java.util.*,
                 org.apache.axis.engine.Phase"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>View Phases</title>
<link href="css/axis-style.css" rel="stylesheet" type="text/css">
</head>
<body>
<h1>Available Phases</h1>
     <%
         ArrayList phases = (ArrayList)request.getSession().getAttribute(Constants.PHASE_LIST);
         ArrayList tempList;
     %><hr><h2><font color="blue">System pre-defined phases</font></h2>
     <b>InFlow Upto Dispatcher</b>
     <blockquote>
         <%
             tempList = (ArrayList)phases.get(0);
             for (int i = 0; i < tempList.size(); i++) {
                 String phase = (String) tempList.get(i);
         %><%=phase%><br><%
             }
         %>
         </blockquote>
         <b>InFaultFlow </b>
         <blockquote>
         <%
             tempList = (ArrayList)phases.get(1);
             for (int i = 0; i < tempList.size(); i++) {
                 String phase = (String) tempList.get(i);
         %><%=phase%><br><%
             }
         %>
         </blockquote>
         <b>OutFlow </b>
         <blockquote>
         <%
             tempList = (ArrayList)phases.get(2);
             for (int i = 0; i < tempList.size(); i++) {
                 String phase = (String) tempList.get(i);
         %><%=phase%><br><%
             }
         %>
         </blockquote>
         <b>OutFaultFlow </b>
         <blockquote>
         <%
             tempList = (ArrayList)phases.get(3);
             for (int i = 0; i < tempList.size(); i++) {
                 String phase = (String) tempList.get(i);
         %><%=phase%><br><%
             }
         %>
         </blockquote>
         <br>
         <hr>
         <h2><font color="blue">User defined phases</font></h2>
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
         </body>
</html>