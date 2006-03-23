<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
<table width="100%" style="border-right:1px solid #CCCCCC;">
    <tr>
     <td colspan="2" >
       <b>Tools </b>
     </td>
    </tr>
    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="upload.jsp">Upload Service</a>
       </td>
    </tr>
<tr>
     <td colspan="2" >
      <b><nobr>System Components&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</nobr></b>
     </td>
  </tr>
    <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td >
      <a href="listService">Available Services</a>
    </td>
 </tr>
 <tr>
    <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td >
      <a href="listServciceGroups">Available Service Groups</a>
    </td>
 </tr>
 <tr>
    <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td >
      <a href="listModules">Available Modules</a>
    </td>
 </tr>
 <tr>
    <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td>
      <a href="globalModules">Globally Engaged Modules</a>
    </td>
 </tr>
 <tr>
    <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td >
      <a href="listPhases">Available Phases</a>
    </td>
 </tr>
  <tr>
     <td colspan="2" >
       <b>Execution Chains</b>
     </td>
  </tr>
   <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="viewGlobalHandlers">Global Chains</a>
       </td>
    </tr>
    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="selectService">Operation Specific Chains</a>
       </td>
    </tr>
    <tr>
     <td colspan="2" >
       <b>Engage Module</b>
     </td>
  </tr>
   <tr>
       <td>
        &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="engagingglobally">For all Services</a>
       </td>
    </tr>
     <tr>
        <td>
         &nbsp;&nbsp;&nbsp;&nbsp;
        </td>
        <td>
          <a href="engageToServiceGroup">For a Service Group</a>
        </td>
     </tr>


    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="engageToService">For a Service</a>
       </td>
    </tr>

     <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="listoperation">For an Operation</a>
       </td>
    </tr>

    <tr>
     <td colspan="2" >
       <b>Services</b>
     </td>
  </tr>
    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="inActivateService">Inactivate Service</a>
       </td>
    </tr>
    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="activateService">Activate Service</a>
       </td>
    </tr>
    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="selectServiceParaEdit">Edit Parameters</a>
       </td>
    </tr>
    <tr>
     <td colspan="2" >
       <b>Contexts</b>
     </td>
    </tr>
    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="listContexts">View Hierarchy</a>
       </td>
    </tr>
</table>