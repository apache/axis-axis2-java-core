<%@ page import="org.apache.axis.Constants,
                 java.util.Hashtable"%>
 <%--
  Created by IntelliJ IDEA.
  User: me
  Date: Jun 7, 2005
  Time: 10:51:02 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Errorness Modules</title></head>
  <body>
  <%
      Hashtable errornessModules =(Hashtable)request.getSession().getAttribute(Constants.ERROR_MODULE_MAP);
      String moduleName = (String)request.getParameter("moduleName");
  %> <h3>The Module has deployment faults</h3><%
  %><font color="red" ><%=(String)errornessModules.get(moduleName) %></font><%
  %>
  </body>
</html>