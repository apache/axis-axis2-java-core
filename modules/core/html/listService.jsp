<%@ page import="org.apache.axis.Constants,
                 java.util.*,
                 org.apache.axis.description.ServiceDescription,
                 org.apache.axis.description.OperationDescription,
                 org.apache.axis.description.ModuleDescription"%>
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
<html>
  <head><title>List Services</title>
  <link href="css/axis-style.css" rel="stylesheet" type="text/css">
  </head>
  <body>
  <h1>Available services</h1>
     <%
         HashMap serviceMap = (HashMap)request.getSession().getAttribute(Constants.SERVICE_MAP);
         Hashtable errornessservice =(Hashtable)request.getSession().getAttribute(Constants.ERROR_SERVICE_MAP);
         boolean status = false;
         if (serviceMap!=null && !serviceMap.isEmpty()){
             HashMap operations;
             String serviceName = "";
             Collection servicecol = serviceMap.values();
             Collection operationsList;
             for (Iterator iterator = servicecol.iterator(); iterator.hasNext();) {
                 ServiceDescription axisService = (ServiceDescription) iterator.next();
                 operations = axisService.getOperations();
                 operationsList = operations.values();
                 serviceName = axisService.getName().getLocalPart();
     %><hr><h2><font color="blue"><%=serviceName%></font></h2>
     <h4>Service Description : <font color="black"><%=axisService.getServiceDescription()%></h4>
           <%
                 Collection engagdeModules = axisService.getEngagedModules();
                 String moduleName = "";
                 if(engagdeModules.size() >0){
           %>
           <i>Engaged Modules for the service</i>
                <%
                     for (Iterator iteratorm = engagdeModules.iterator(); iteratorm.hasNext();) {
                         ModuleDescription description = (ModuleDescription) iteratorm.next();
                         moduleName = description.getName().getLocalPart();
                %><li><%=moduleName%></li>
                         <br>
                         <%
                     }
                 }
                 if (operationsList.size() > 0) {
                %><br><i>Available operations</i><%
                 } else {
                %><i> There are no any opeartions specified</i><%
                 }
                %><ul><%
                 for (Iterator iterator1 = operationsList.iterator(); iterator1.hasNext();) {
                     OperationDescription axisOperation = (OperationDescription) iterator1.next();
                %><li><%=axisOperation.getName().getLocalPart()%></li><%
                     engagdeModules = null;
                     engagdeModules = axisOperation.getModules();
                     moduleName = "";
                     if(engagdeModules.size() >0){
                %>
                <br><i>Engaged Modules for the opeartion</i><ul>
                <%
                         for (Iterator iterator2 = engagdeModules.iterator(); iterator2.hasNext();) {
                             ModuleDescription description = (ModuleDescription) iterator2.next();
                             moduleName = description.getName().getLocalPart();
                %><li><%=moduleName%></li><br><%
                         }
                         %></ul><%
                     }

                 }
                %></ul>
           <%
                 status = true;
             }
         }
               if(errornessservice != null){
                   if(errornessservice.size() > 0){
                       request.getSession().setAttribute(Constants.IS_FAULTY,Constants.IS_FAULTY);
           %>
           <hr><h3><font color="blue">Faulty Services</font></h3>
                <%
                       Enumeration faultyservices = errornessservice.keys();
                       while (faultyservices.hasMoreElements()) {
                           String faultyserviceName = (String) faultyservices.nextElement();
                %><h3><font color="blue"><a href="listSingleService.jsp?serviceName=<%=faultyserviceName%>">
                    <%=faultyserviceName%></a></font></h3>
                    <%
                       }
                   }
                   request.getSession().removeAttribute(Constants.SERVICE_MAP);
                   status = true;
               }if(!status){
                    %> There seems to be no services listed! Try hitting refresh <%
               }
                    %>
  </body>
</html>