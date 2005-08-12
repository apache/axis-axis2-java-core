======================================================
Apache Axis2 0.91 build  (August 15, 2005)

http://ws.apache.org/axis2
------------------------------------------------------

___________________
Documentation
===================
 
Documentation can be found in the xdocs/docs directory included with both the 
source and binary distributions.

___________________
Installation
===================

The binary distribution contains a WAR file (axis2.war)  to ease 
installation into a servlet container: simply copy the WAR file 
to the servlet container's webapps directory and you're ready to go.

To verify the installation, go to http://localhost:8080/axis2/ and 
click on the the "Validate" link, where the host name and port 
of that URL should be changed as appropriate.
 
To build WAR file (axis2.war) using the source distribution use the 
following command:
    $ maven war 

___________________
Deploying
===================

To deploy a new Web service in Axis2 the following three steps must 
be performed:
  1) Create the Web service implementation class, supporting classes 
     and the service.xml file, 
  2) Archive the class files into a jar with the service.xml file in 
     the META-INF
  3) Drop the jar file to the $AXIS_HOME/WEB-INF/services directory
where $AXIS_HOME represents the install directory of your Axis2 
runtime. (In the case of a servelet container this would be the
"axis2" directory inside "webapps".


To verify the deployment please go to http://localhost:8080/axis2/ and 
follow the "List of available services" Link.

For more information please refer to the User's Guide.

___________________
Support
===================
 
Any problem with this release can be reported to Axis the mailing list 
or in the Jira issue tracker. If you are sending email to the mailing 
list make sure to add the [Axis2] prefix to the subject.

Mailing list subscription:
    axis-dev-subscribe@ws.apache.org

Jira:
    http://issues.apache.org/jira/secure/BrowseProject.jspa?id=10611


Thank you for your support of Axis2!

The Axis2 Team. 
