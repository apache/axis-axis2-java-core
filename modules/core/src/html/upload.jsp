<%@ page import="java.io.DataInputStream,
                 java.io.File,
                 java.io.FileOutputStream,
                 java.io.Writer,
                 java.io.PrintWriter,
                 java.io.IOException,
                 org.apache.axis.deployment.DeploymentConstants"%>
<%@ page contentType="text/html;charset=UTF-8" language="java"
 %>
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

<%!
    public void jspInit(){
        ServletContext context = this.getServletConfig().getServletContext();
        File repoDir = new File(context.getRealPath("/WEB-INF"));
        File serviceDir = new File(repoDir,DeploymentConstants.SERVICE_PATH);

        if (!serviceDir.exists()) {
            serviceDir.mkdir();
        }
        deploymentDirectory = serviceDir;
    }

    protected static final String SUBMIT_NAME = "upload";
    protected File deploymentDirectory = null;

    protected void writeSuccessMessage(String fileName,JspWriter out) throws IOException {
        out.print("File saved as " +fileName + "<br/>");
    }

    protected void writeUnsuccessMessage(String message,JspWriter out) throws IOException{
        out.print("<font color=\"red\">The following error occured <br/>" +message +
                "</font><br/>"
        );

    }
%>
<head>
 <title> Upload a service </title>
  <link href="css/axis-style.css" rel="stylesheet" type="text/css">
</head>
	<body>
	    <jsp:include page="include/header.inc"></jsp:include>
        <%
            String contentType = request.getContentType();
            if (contentType!=null && contentType.indexOf("multipart/form-data")>=0){

                DataInputStream in = new DataInputStream(request.getInputStream());
                int formDataLength = request.getContentLength();
                File outputFile = null;
                byte dataBytes[] = new byte[formDataLength];
                int byteRead = 0;
                int totalBytesRead = 0;

                while (totalBytesRead < formDataLength) {
                    byteRead = in.read(dataBytes, totalBytesRead, formDataLength);
                    totalBytesRead += byteRead;
                }

                String file = new String(dataBytes);
                String saveFile = file.substring(file.indexOf("filename=\"") + 10);
                saveFile = saveFile.substring(0, saveFile.indexOf("\n"));
                saveFile = saveFile.substring(saveFile.lastIndexOf("\\") + 1,saveFile.indexOf("\""));
                saveFile = saveFile.toLowerCase();

                if (!saveFile.endsWith(".jar")){
                    writeUnsuccessMessage("Wrong file type!!",out);
                    %>
                    <jsp:include page="include/link-footer.inc"></jsp:include>
                    <jsp:include page="include/footer.inc"></jsp:include><%
                    return;
                }

                outputFile = new File(deploymentDirectory,saveFile);

                int lastIndex = contentType.lastIndexOf("=");
                String boundary = contentType.substring(lastIndex + 1,contentType.length());

                int pos;
                pos = file.indexOf("filename=\"");
                pos = file.indexOf("\n", pos) + 1;
                pos = file.indexOf("\n", pos) + 1;
                pos = file.indexOf("\n", pos) + 1;

                int boundaryLocation = file.indexOf(boundary, pos) - 4;
                int startPos = ((file.substring(0, pos)).getBytes()).length;
                int endPos = ((file.substring(0, boundaryLocation)).getBytes()).length;

                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                //fileOutputStream.write(dataBytes);
                fileOutputStream.write(dataBytes, startPos, (endPos - startPos));
                fileOutputStream.flush();
                fileOutputStream.close();

                writeSuccessMessage(saveFile,out);

            }else{
        %>
		<h2>Upload a service jar file</h2>
		You can upload a properly packaged Axis 2 service by selecting the file and clicking upload
       <form method="post"  name="Axis2upload" action="upload.jsp" enctype="multipart/form-data">
         <input type="file" name="filename" chars="50"/><br/>
         <input name="<%=SUBMIT_NAME%>" type="submit" value=" Upload "/>
       </form>
       <%}%>
       <jsp:include page="include/link-footer.inc"></jsp:include>
       <jsp:include page="include/footer.inc"></jsp:include>
	</body>
</html>