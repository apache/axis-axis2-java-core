<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.AxisService,
                 java.util.Collection,
                 java.util.HashMap,
                 java.util.Iterator"%>
  <%
           /*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*
*/

/**
 * Author : Deepal Jayasinghe
 * Date: May 26, 2005
 * Time: 7:14:26 PM
 */
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/adminheader.jsp"></jsp:include>

  <%
      String action ="";
      String buttonName="" ;
      String status = (String)request.getSession().getAttribute(Constants.SELECT_SERVICE_TYPE);
      String heading = "";
      String disc = "";
      if(status != null && status.equals("MODULE")) {
          action = "listOperations";
          buttonName = " View Operations";
          heading = "Select a Service to view Operation specific Chains";
          disc = "Select a axisService from the combo and click on the 'View Operations' button to view Operation specific Chains.";
      } else if(status != null && status.equals("VIEW")){
          buttonName = " View ";
          action = "viewServiceHandlers";
          heading = "Select a Service to view Service Handlers";
          disc = "Select a axisService from the combo and click on the 'View' button to view Service Handlers.";
      } else if (status != null && status.equals("SERVICE_PARAMETER")){
          buttonName = " Edit Parameters ";
          action = Constants.EDIR_SERVICE_PARA;
          heading = "Select a Service to Edit Parameters";
          disc = "Select a axisService from the combo and click on the 'Edit Parameters' button to Edit Parameters.";
      }
  %>
<h1><%=heading%></h1>
<p><%=disc%></p>
<form method="get" name="selectServiceForm" action="axis2-admin/<%=action%>">
<table border="0" width="50%" cellspacing="1" cellpadding="1">
         <tr>
        <td width="35%">Select a Service :</td><td width="65%">
            <select name="axisService">
            <%
                HashMap services = (HashMap)request.getSession().getAttribute(Constants.SERVICE_MAP);
                Collection serviceCol =  services.values();
                for (Iterator iterator = serviceCol.iterator(); iterator.hasNext();) {
                    AxisService axisService = (AxisService)iterator.next();
                    String serviceName = axisService.getName();
            %> <option  align="left" value="<%=serviceName%>"><%=serviceName%></option>
             <%
                }
             %>
           </td>
        </tr>
	<tr><td colspan="2">&nbsp;</td></tr>
        <tr><td>&nbsp;</td>
             <td colspan="2" align="left">
                <input name="submit" type="submit" value="<%=buttonName%>" >
             </td>
         </tr>
      </table>
   </form>
<jsp:include page="include/adminfooter.jsp"></jsp:include>
