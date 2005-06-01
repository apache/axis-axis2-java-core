<%@ page import="java.util.Collection,
                 org.apache.axis.Constants,
                 java.util.Iterator,
                 javax.xml.namespace.QName,
                 org.apache.axis.description.ModuleDescription,
                 java.util.HashMap,
                 java.util.Enumeration"%><html>
<head>
<title>Engaging Module Globally</title>
<style type="text/css">
</style></head>

<body>
<%
    String status = (String)request.getSession().getAttribute(Constants.ENGAGE_STATUS);
%>



<form method="get" name="engaginModule" action="engagingglobally">
<table border="0" width="100%" cellspacing="1" cellpadding="1">
<tr>
<td>
&nbsp;
&nbsp;
</td>
</tr>
<tr>
<td>Select a Module :</td>
</tr><tr>
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


