<%@ page import="org.apache.axis2.Constants"%>        <%@ page contentType="text/html;charset=UTF-8" language="java" %>
        <html>
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
  <%
      String status = (String)request.getSession().getAttribute(Constants.LOGGED);
      if(status == null || (! status.equals("Yes"))) {
          throw new Exception("Invalid logging");
      }
  %>
<jsp:include page="include/adminheader.jsp"></jsp:include>
<h1>Welcome to the Axis2 administration system!</h1><p>There are several things that you can do from within this system. You can check the status of your axis system from these pages.
There are also some functionality provided to change some internel setting while the system is online.
<jsp:include page="include/adminfooter.jsp"></jsp:include>
