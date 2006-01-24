<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="org.apache.axis2.Constants,
                                                                             org.apache.axis2.description.AxisServiceGroup,
                                                                             org.apache.axis2.description.AxisModule,
                                                                             java.util.Collection" errorPage="" %>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Iterator"%>
<jsp:include page="include/adminheader.jsp"></jsp:include>
<%
    String status = (String)request.getSession().getAttribute(Constants.ENGAGE_STATUS);
    HashMap moduels = (HashMap)request.getSession().getAttribute(Constants.MODULE_MAP);
    Collection moduleCol =  moduels.values();
    Iterator servicesGroups = (Iterator)request.getSession().getAttribute(Constants.SERVICE_GROUP_MAP);
%>
<h1>Engage Module for a Service Group</h1>
<p>To engage a module for a set of services grouped as a axisService group, first select the module you want to engage and then select the axisService group you like the module to be engaged on and click "Engage".</p>
<%
	if (!moduleCol.iterator().hasNext()) {%>
		<p>No modules are present to be engaged.</p>
	<%} else {
		if  (!servicesGroups.hasNext()) {%>
		<p>No axisService groups are present to be engaged.</p>
		<%} else {
%>
<form method="get" name="selectModuleForm" action="engageToServiceGroup">
    <table border="0" width="100%" cellspacing="1" cellpadding="1">
        <tr>
            <td>Select a Module :</td>
        </tr>
        <tr>
            <td>
                <select name="modules">
                    <%
                for (Iterator iterator = moduleCol.iterator(); iterator.hasNext();) {
                    AxisModule axisOperation = (AxisModule) iterator.next();
                    String modulename = axisOperation.getName().getLocalPart();
            %> <option  align="left" value="<%=modulename%>"><%=modulename%></option>
                    <%
                }
             %>
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>Select a Service Group :</td>
        </tr>
        <tr>
            <td>
                <select name="axisService">
                    <%

                        while(servicesGroups.hasNext()){
                            AxisServiceGroup axisServiceGroup = (AxisServiceGroup) servicesGroups.next();
                            String serviceName = axisServiceGroup.getServiceGroupName();
                    %> <option  align="left" value="<%=serviceName%>"><%=serviceName%></option>
                    <%
                        }

                    %>
                </select>
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>
                <input name="submit" type="submit" value=" Engage " >
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>
                <textarea cols="50"  <%
                    if(status == null){
                %>
                          style="display:none"
                        <%
                            } %>
                        ><%=status%></textarea>
            </td>
        </tr>
    </table>
</form>
<%
		}
	}
%>
<jsp:include page="include/adminfooter.jsp"></jsp:include>

