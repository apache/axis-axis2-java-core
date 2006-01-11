<%@ page import="org.apache.commons.fileupload.DiskFileUpload,
                 org.apache.commons.fileupload.FileItem,
                 org.apache.commons.fileupload.FileUpload,
                 javax.servlet.ServletContext, javax.servlet.jsp.JspWriter"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="org.apache.commons.fileupload.FileUpload"%>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload"%>
<%@ page import="org.apache.commons.fileupload.FileItem"%>
<%@ page contentType="text/html;charset=UTF-8" language="java"
 %>
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
        out.print("<font color=\"red\">The following error occurred <br/>" +message +
                "</font><br/>"
        );

    }
%>
<jsp:include page="include/adminheader.jsp"></jsp:include>
         <h2>Upload a axisService jar file</h2>
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
                            String fileExtesion =fileName;
                            fileExtesion =fileExtesion.toLowerCase();
                            if (!(fileExtesion.endsWith(".jar")||fileExtesion.endsWith(".aar"))){
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

        <p>You can upload a packaged Axis 2 axisService using this page with two small steps.</p>
	<ul><li>Browse to the location and select the axisService archive file you wish to upload</li>
	<li>Click Upload</li></ul>
	<p>Simple as that!</p>
<form method="post"  name="Axis2upload" action="upload.jsp" enctype="multipart/form-data">
        <table><tr><td>Service archive : </td><td>
        <input type="file" name="filename" size="50"/></td></tr>
        <tr><td>&nbsp;</td><td><input name="<%=SUBMIT_NAME%>" type="submit" value=" Upload "/></td></tr>
	</table>
</form>
<jsp:include page="include/adminfooter.jsp"></jsp:include>
