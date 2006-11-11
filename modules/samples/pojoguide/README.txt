POJO Web Services using Apache Axis2- Sample 1
=============================================

Introduction
============

This sample shows how to expose a Java class as a web service.  
The WeatherService Java class provides methods to get and set a Weather 
type Java objects. The client uses RPCServiceClient to invoke those two 
methods just as Java object method invocation.

Pre-Requisites
==============

Apache Ant 1.6.2 or later

Building the Service

type:
$ant


Running the Client
==================
type:
$ant rpc.client -Duri=http://<your host>:<your port>/axis2/services/WeatherService


Help
====
Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble running the sample.