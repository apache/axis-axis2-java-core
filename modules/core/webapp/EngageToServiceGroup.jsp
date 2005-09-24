<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="org.apache.axis2.Constants,
                                                                             org.apache.axis2.description.ModuleDescription,
                                                                             org.apache.axis2.description.ServiceDescription,
                                                                             java.util.Collection,
                                                                             java.util.HashMap,
                                                                             java.util.Iterator" errorPage="" %>
<%@ page import="org.apache.axis2.description.ServiceGroupDescription"%>
<jsp:include page="include/adminheader.jsp"></jsp:include>
<%
    String status = (String)request.getSession().getAttribute(Constants.ENGAGE_STATUS);
%>
<h1>Engage Module for a Service Group</h1>
<form method="get" name="engaginModule" action="engageToServiceGroup">
    <table border="0" width="100%" cellspacing="1" cellpadding="1">
        <tr>
            <td>
                &nbsp;
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>Select a Module :</td>
        </tr>
        <tr>
            <td>
                <select name="modules">
                    <%
                HashMap moduels = (HashMap)request.getSession().getAttribute(Constants.MODULE_MAP);
                Collection moduleCol =  moduels.values();
                for (Iterator iterator = moduleCol.iterator(); iterator.hasNext();) {
                    ModuleDescription description = (ModuleDescription) iterator.next();
                    String modulename = description.getName().getLocalPart();
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
                <select name="service">
                    <%
                        Iterator servicesGroups = (Iterator)request.getSession().getAttribute(Constants.SERVICE_GROUP_MAP);
                        while(servicesGroups.hasNext()){
                            ServiceGroupDescription groupDescription = (ServiceGroupDescription) servicesGroups.next();
                            String servicName = groupDescription.getServiceGroupName();
                    %> <option  align="left" value="<%=servicName%>"><%=servicName%></option>
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
<jsp:include page="include/adminfooter.jsp"></jsp:include>

