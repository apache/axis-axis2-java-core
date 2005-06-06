<%@ page import="java.io.DataInputStream,
                 java.io.File,
                 java.io.FileOutputStream,
                 java.io.Writer,
                 java.io.PrintWriter,
                 java.io.IOException,
                 org.apache.commons.fileupload.FileUpload,
                 java.util.List,
                 org.apache.commons.fileupload.DiskFileUpload,
                 org.apache.commons.fileupload.FileItem,
                 java.util.Iterator"%>
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
        File serviceDir = new File(repoDir,"services");

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
         <h2>Upload a service jar file</h2>
        <%
            boolean isMultipart = FileUpload.isMultipartContent(request);
            if (isMultipart){
                try {
                    // Create a new file upload handler
                    DiskFileUpload upload = new DiskFileUpload();

                    List items = upload.parseRequest(request);

                    // Process the uploaded items
                    Iterator iter = items.iterator();
                    while (iter.hasNext()) {
                        FileItem item = (FileItem) iter.next();

                        if (!item.isFormField()) {

                            String fileName = item.getName();
                            fileName = fileName.toLowerCase();
                            if (!(fileName.endsWith(".jar")||fileName.endsWith(".aar"))){
                                throw new Exception(" Wrong file type! ");
                            }

                            String fileNameOnly = "";
                            if (fileName.indexOf("\\")<0){
                                  fileNameOnly= fileName.substring(fileName.lastIndexOf("/")+1,fileName.length());
                            }else{
                                 fileNameOnly= fileName.substring(fileName.lastIndexOf("\\")+1,fileName.length());
                            }


                            File uploadedFile = new File(deploymentDirectory,fileNameOnly);
                            item.write(uploadedFile);
                            out.write("<font color=\"green\">File " + fileName + " successfully uploaded </font><br/><br/>");

                        }
                    }
                } catch (Exception e) {
                    out.write(" <font color=\"red\">File upload failed! <br/>" + e.getMessage() + "</font><br/><br/>");
                }
            }
        %>

        You can upload a properly packaged Axis 2 service by selecting the file and clicking upload
        <form method="post"  name="Axis2upload" action="upload.jsp" enctype="multipart/form-data">
        <input type="file" name="filename" chars="50"/><br/>
        <input name="<%=SUBMIT_NAME%>" type="submit" value=" Upload "/>
        </form>

       <jsp:include page="include/link-footer.inc"></jsp:include>
       <jsp:include page="include/footer.inc"></jsp:include>
       </body>
</html>