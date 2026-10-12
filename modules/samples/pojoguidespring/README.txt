POJO Web Services using Apache Axis2- Sample 2
==============================================
This sample contains source code for the POJO Web Services guide. For a more detailed
description on the source code, see the 'POJO Web Services using Apache Axis2' documentation.

In this specific sample you'll be shown how to take a POJO  (Plain Old Java Object) 
based on the Spring Framework, and deploy that as an AAR packaged Web service on Tomcat. 
This is a quick way to get a Web service up and running in no time. 

Introduction
============

This sample shows how to expose the getters and setters of WeatherSpringService that 
takes Weather type Java Object as the argument and the return type. It uses the Spring 
framework to initialize the weather property of the WeatherSpringService.


Pre-Requisites
==============

Apache Ant 1.10 or later

Spring Framework 7.x jars (Axis2 is built against Spring 7.0.9). The binary
distribution ships axis2-spring but not Spring itself. The build downloads spring-core,
spring-beans, spring-context, spring-aop and spring-expression 7.0.9 from Maven Central
into this sample's lib/ directory the first time it runs. To run the service, the same
jars must also be on the server's class path: copy them from lib/ into Axis2_HOME/lib
(or the axis2 webapp's WEB-INF/lib).

Building the Service
====================

Type $ant from Axis2_HOME/modules/samples/pojoguidespring


Running the Client
==================
Type $ant rpc.client from Axis2_HOME/modules/samples/pojoguidespring

Help
====
Please contact java-user list (java-user@axis.apache.org) if you have any trouble running the sample.