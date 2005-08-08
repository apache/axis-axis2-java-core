<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="org.apache.axis2.Constants,
                                                                             org.apache.axis2.description.ModuleDescription,
                                                                             org.apache.axis2.description.OperationDescription,
                                                                             java.util.Collection,
                                                                             java.util.HashMap,
                                                                             java.util.Iterator" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Untitled Document</title>
<style type="text/css">
</style></head>

<body>
<body>
<%
    String status = (String)request.getSession().getAttribute(Constants.ENGAGE_STATUS);
%>
<form method="get" name="engaginModule" action="listOperations">
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
        <td>Select an Operation :</td>
    </tr>
    <tr>
        <td>
            <select name="operation">
            <%
                HashMap operations = (HashMap)request.getSession().getAttribute(Constants.OPEARTION_MAP);
                Collection serviceCol =  operations.values();
                for (Iterator iterator = serviceCol.iterator(); iterator.hasNext();) {
                    OperationDescription operation = (OperationDescription)iterator.next();
                    String opname = operation.getName().getLocalPart();
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
</body>
</html>


