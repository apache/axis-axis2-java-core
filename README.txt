======================================================
Apache Axis2 milestone build  (Feb 22, 2005)

http://ws.apache.org/axis2
------------------------------------------------------

___________________
Documentation
===================
 
 Documentation can be found in the docs directory of the source or binary distribution

___________________
Installation
===================

 Axis2 Binary distribution contains axis2.war in the webapps directory. This will ease the installation in a Servlet contianer. 
 The installation in the Servlet container is just a matter of coping the axis2.war to the webapps directory. 
 
 To verify the installation, go to http://127.0.0.1:8080/axis2/ and try the "Validate" link
 
 To build the axis2.war using source distribution use the following command.
 $ maven war 

 
 
___________________
Deploying
===================

  To deploy a new Web Service in  Axis2 require three steps
  1) Create Web Service implementation class, supporting classes and the service.xml file, 
  2) Archive the class files into a jar with the service.xml file in the META-INF
  3) Drop the jar file to the $AXIS_HOME/WEB-INF/services directory

 To verify the deployment please go to http://127.0.0.1:8080/axis2/ and follow the
 "List Available services" Link 

 For more information please refer to the User Guide

___________________
Support
===================
 
 Any problem with this release can be reported to Axis 
 mailing list or Jira issue tracker. If you are sending a mail to mailing list make sure 
 to add the [Axis2] prefix to the subject.

 
 Mailing list subscription:
 axis-dev-subscribe@ws.apache.org

 Jira:
 http://issues.apache.org/jira/secure/BrowseProject.jspa?id=10611

 
