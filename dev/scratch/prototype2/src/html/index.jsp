<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
    <title>Axis 2 - Home</title>
    <link href="css/axis-style.css" rel="stylesheet" type="text/css">
</head>
	<body>
        <jsp:include page="include/header.inc"></jsp:include>
       	<br/>
       	    Welcome to the new generation of Axis. If you can see this it means you have
       	    successfuly deployed the Axis 2 web application. However to ensure that Axis 2
       	    is properly working, we encourage you to go to the validate link.
       	<br/>
       	<ul>
         <li><a href="listServices">List Available services</a>
            <br/>
            Lists all the available services.
         </li>
         <li><a href="HappyAxis.jsp">Validate</a>
         <br/>
            This will probe the system to see whether all the required libraries are in place.
            It will also provide the system information.
         </li>
         <li><a href="upload.jsp">Upload a service</a>
         <br/>
            Upload a service. you can upload a properly packaged service here
         </li>
        </ul>

        <jsp:include page="include/footer.inc"></jsp:include>
	</body>
</html>