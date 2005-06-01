<%@ page import="org.apache.axis.Constants"%>        <%@ page contentType="text/html;charset=UTF-8" language="java" %>
        <html>
        <%
            /*
            * Copyright 2002,2004 The Apache Software Foundation.
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
            */
        %>
        <head>
<title>Axis2 :: Administrations Page</title>
</head>

<frameset rows="200,*" frameborder="NO" border="0" framespacing="0">
<frame src="TopFrame.jsp" name="topFrame" scrolling="NO"  >
<frameset rows="*" cols="245,*" framespacing="0" frameborder="NO" border="0">
<frame src="LeftFrame.jsp" name="leftFrame"  border="0" scrolling="NO"  noresize>
<frame src="MainFrame.jsp"  border="0" name="mainFrame">
</frameset>
</frameset>
<noframes><body>
  <%
      String status = (String)request.getSession().getAttribute(Constants.LOGGED);
      if(status == null || (! status.equals("Yes"))) {
          throw new Exception("Invalid logging");
      }
  %>
</body></noframes>
</html>