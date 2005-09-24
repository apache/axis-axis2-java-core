<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.OperationDescription,
                 org.apache.axis2.description.Parameter,
                 org.apache.axis2.description.ServiceDescription,
                 java.util.ArrayList,
                 java.util.Collection,
                 java.util.HashMap,
                 java.util.Iterator"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/adminheader.jsp"></jsp:include>
<h1>Edit Service Parameters</h1>
  <form method="get" name="editServicepara" action="editServicepara">
   <%
            ServiceDescription service = (ServiceDescription)request.getSession().
                    getAttribute(Constants.SERVICE);
             if(service != null ){
   %>     <table width="100%">

            <tr>
                 <td colspan="2" ><b>
           <%
                 String servicName =  service.getName().getLocalPart();
                 %>Service Parameters :: <%=servicName%>
                 </b></td>
             </tr>
             <tr>
             <td colspan="2" ><input style="display:none"  name="service" value="<%=servicName%>"></td>
            </tr>
             <%
                 ArrayList service_para = service.getParameters();
                 for (int i = 0; i < service_para.size(); i++) {
                     Parameter parameter = (Parameter) service_para.get(i);
                     %>
                     <tr>
                     <td><%=parameter.getName()%></td>
                     <td><input type="text" value="<%=parameter.getValue()%>"
                           name="<%=(servicName + "_" + parameter.getName())%>" size="50">
                           </td>
                     </tr>
                     <%
                 }
                HashMap operations =  service.getOperations();
                Collection ops = operations.values();
                if(ops.size() >0){
                    %>
                    <tr>
                      <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
                      <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
                    </tr>
                    <tr>
                       <td colspan="2" > <b>Operation Paramaters :: </b>
                       </td>
                    </tr>
                    <%
                }

                 ArrayList op_paras ;
                 for (Iterator iterator = ops.iterator(); iterator.hasNext();) {
                     OperationDescription description = (OperationDescription) iterator.next();
                     String opeartionName = description.getName().getLocalPart();
                     %>
                     <tr>
                       <td colspan="2" > &nbsp;&nbsp;&nbsp;&nbsp;</td>
                     </tr>
                     <tr>
                       <td colspan="2" ><b>Operation : <%=opeartionName%></b></td>
                     </tr>
                    <%
                     op_paras = description.getParameters();
                     for (int i = 0; i < op_paras.size(); i++) {
                     Parameter parameter = (Parameter) op_paras.get(i);
                     %>
                     <tr>
                     <td><%=parameter.getName()%></td>
                     <td><input type="text" value="<%=parameter.getValue()%>"
                           name="<%=(opeartionName + "_" + parameter.getName())%>" size="50">
                           </td>
                     </tr>
                     <%
                  }
                 }
                 %>
                 <tr>
                    <td>&nbsp;</td>
                <td>
                     <input name="changePara" type="submit" value=" Change " >
               </td>
               </tr>
                 </table>
                 <%
             }

       %>
       <form>
<jsp:include page="include/adminfooter.jsp"></jsp:include>
