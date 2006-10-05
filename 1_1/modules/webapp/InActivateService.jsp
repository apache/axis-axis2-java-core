<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.AxisService,
                 java.util.Collection,
                 java.util.HashMap,
                 java.util.Iterator"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/adminheader.jsp"></jsp:include>
<h1>Turn Off Service</h1>
<form method="get" name="serviceInActivate" action="axis2-admin/deactivateService">
  <table width="100%"  border="0">
<tr>
  <td colspan="2" >
     <p>Only the services that are active are listed below. Note that although you can activate a service from this page,once system is restarted the service will be active again</p>
<%--    <b>Remove Service :</b> The selected axisService will be removed from the file system and if the--%>
<%--    &nbsp; system restart it wont be there next time--%>
  </td>
  </tr>
  <tr>
    <td width="20%"> Select Service : </td>
    <td width="80%">
       <select name="axisService" class="selectBoxes" >

                           <%
                       HashMap services = (HashMap)request.getSession().getAttribute(Constants.SERVICE_MAP);
                       Collection col = services.values();
                       for (Iterator iterator = col.iterator(); iterator.hasNext();) {
                           AxisService axisServices = (AxisService) iterator.next();
                           if(axisServices.isActive()){
                               %> <option value="<%=axisServices.getName()%>">
                           <%=axisServices.getName()%></option> <%
                           }
                       }
                       request.getSession().setAttribute(Constants.SERVICE_MAP,null);
                           %>
                  </td>
  </tr>
  <tr>
    <td width="20%">Inactivate Service </td>
    <td width="80%"><input type="checkbox" name="turnoff">
    </td>
  </tr>
  <tr>
  <td>&nbsp;</td>
  <td>
    <input name="submit" type="submit" value=" Inactivate " >
   <input name="reset" type="reset" value=" Clear " >
  </td>
  </tr>

</table>
</form>
<jsp:include page="include/adminfooter.jsp"></jsp:include>
