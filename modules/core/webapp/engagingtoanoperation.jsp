<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="org.apache.axis2.Constants,
                                                                             org.apache.axis2.description.AxisOperation,
                                                                             org.apache.axis2.description.ModuleDescription,
                                                                             java.util.Collection,
                                                                             java.util.HashMap,
                                                                             java.util.Iterator" errorPage="" %>
<jsp:include page="include/adminheader.jsp"></jsp:include>
<%
    String status = (String)request.getSession().getAttribute(Constants.ENGAGE_STATUS);
%>
<h1>Engage Module for an Operation</h1>
<p>To engage a module for a axisOperation, first select the module you want to engage and then select the axisOperation you like the module to be engaged on and click "Engage".</p>
<form method="get" name="selectModuleForm" action="listOperations">
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
                    ModuleDescription axisOperation = (ModuleDescription) iterator.next();
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
        <td>Select an Operation :</td>
    </tr>
    <tr>
        <td>
            <select name="axisOperation">
            <%
                HashMap operations = (HashMap)request.getSession().getAttribute(Constants.OPERATION_MAP);
                Collection serviceCol =  operations.values();
                for (Iterator iterator = serviceCol.iterator(); iterator.hasNext();) {
                    AxisOperation axisOperationtion = (AxisOperation)iterator.next();
                    String opname = axisOperationtion.getName().getLocalPart();
            %> <option  align="left" value="<%=opname%>"><%=opname%></option>
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

