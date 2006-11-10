Sample: ServiceLifeCycle
=========================================================

Introduction:
============

This sample demonstrate usage of service lifecycle and bit of session managment. The main idea is to show where and how do you use service lifecycle interface and session related methods. 


Prerequisites
=============
Apache Ant 1.6.2 or later
If you want to access the service in REST manner you have to deploy the service in tocat like application server. And it will not work in SimpleHttpServer.


Running the Sample:
===================

Deploying the sevrice  : 
  Deploy into tomcat :
     to build and copy the service arcive file into tomcat just run  copy.to.tomcat goal which will copy the aar file into
tomcat/web-app/axis2/WEB-INF/services directory.
   Deploy into Sample repositor
      run ant generate.service

Running the client :
invoke
ant run.client

And then follow the instructions as mentioned in the console.


Help
====
Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble running the sample.

