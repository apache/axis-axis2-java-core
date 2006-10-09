<%@ page import="org.apache.axis2.Constants,
                 java.util.Hashtable"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/adminheader.jsp"></jsp:include>
  <%
      Hashtable errornessModules =(Hashtable)request.getSession().getAttribute(Constants.ERROR_MODULE_MAP);
      String moduleName = request.getParameter("moduleName");
  %> <h3>The Module has deployment faults</h3><%
  %><font color="red" ><%=(String)errornessModules.get(moduleName) %></font><%
  %>
<jsp:include page="include/adminfooter.jsp"></jsp:include>