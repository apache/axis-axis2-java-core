<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.ServiceDescription,
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
<h1>Select a Service to view Operation specific Chains</h1>
  <%
      String action ="";
      String buttonName="" ;
      String status = (String)request.getSession().getAttribute(Constants.SELECT_SERVICE_TYPE);
      if(status != null && status.equals("MODULE")) {
          action = "listOperations";
          buttonName = " View Operations";
      } else if(status != null && status.equals("VIEW")){
          buttonName = " View ";
          action = "viewServiceHandlers";
      } else if (status != null && status.equals("SERVICE_PARAMETER")){
          buttonName = " Edit Parameters ";
          action = Constants.EDIR_SERVICE_PARA;
      }
  %>
<form method="get" name="engaginModule" action="<%=action%>">
<table border="0" width="100%" cellspacing="1" cellpadding="1">
         <tr>
        <td width="20%">Select a Service :&nbsp;
            <select name="service">
            <%
                HashMap services = (HashMap)request.getSession().getAttribute(Constants.SERVICE_MAP);
                Collection serviceCol =  services.values();
                for (Iterator iterator = serviceCol.iterator(); iterator.hasNext();) {
                    ServiceDescription axisService = (ServiceDescription)iterator.next();
                    String servicName = axisService.getName().getLocalPart();
            %> <option  align="left" value="<%=servicName%>"><%=servicName%></option>
             <%
                }
             %>
           </td>
        </tr>
        <tr>
             <td>
                <input name="submit" type="submit" value="<%=buttonName%>" >
             </td>
         </tr>
      </table>
   </form>
<jsp:include page="include/adminfooter.jsp"></jsp:include>