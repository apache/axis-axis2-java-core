======================================================
Apache Axis2 0.93 build  (December 2, 2005)

http://ws.apache.org/axis2
------------------------------------------------------

___________________
Documentation
===================
 
Documentation can be found in the 'docs' directory included with the 
binary distribution and in the 'xdocs' directory in the source 
distribution.

___________________
Deploying
===================

To deploy a new Web service in Axis2 the following three steps must 
be performed:
  1) Create the Web service implementation class, supporting classes 
     and the services.xml file, 
  2) Archive the class files into a jar with the services.xml file in 
     the META-INF
  3) Drop the jar file to the $AXIS_HOME/WEB-INF/services directory
where $AXIS_HOME represents the install directory of your Axis2 
runtime. (In the case of a servelet container this would be the
"axis2" directory inside "webapps".)

To verify the deployment please go to http://localhost:8080/axis2/ and 
follow the "Services" Link.

For more information please refer to the User's Guide.

___________________
Support
===================
 
Any problem with this release can be reported to Axis the mailing list 
or in the JIRA issue tracker. If you are sending email to the mailing 
list make sure to add the [Axis2] prefix to the subject.

Mailing list subscription:
    axis-dev-subscribe@ws.apache.org

Jira:
    http://issues.apache.org/jira/secure/BrowseProject.jspa?id=10611


Thank you for using Axis2!

The Axis2 Team. 
