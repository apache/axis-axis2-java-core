Sample: JAX-WS top-down development
===================================

Introduction
============
This sample demonstrates top-down development (i.e. development starting with a WSDL document) of a service and client using the JAX-WS API.  The sample is based on a Microsoft SOAP interop scenario for base data types (the original interop lab site is no longer online).

Building the service from scratch
=================================
The service may be constructed from scratch by executing the wsimport tool (from the Jakarta XML Web Services implementation, Eclipse Metro, or the jaxws-maven-plugin) on the src/main/resources/META-INF/BaseDataTypesDocLitB.wsdl file: 'wsimport -keep -verbose BaseDataTypesDocLitB.wsdl'  (The -keep option tells the tool to keep the generated files and the -verbose option causes the tool to list what is generated.)  After the files have been generated you will need to provide an implementation of the service (i.e. a class equivalent to InteropSampleService.java).

Deploying the service
=====================
The classes can be packaged into a jar and the jar can be dropped into the servicejars directory of a deployed axis2 server.

Building the client from scratch
================================
The dynamic proxy client may be constructed from scratch by executing the wsimport tool (from the Jakarta XML Web Services implementation, Eclipse Metro, or the jaxws-maven-plugin) on the src/main/resources/META-INF/BaseDataTypesDocLitB.wsdl file: 'wsimport -keep -verbose BaseDataTypesDocLitB.wsdl'  (The -keep option tells the tool to keep the generated files and the -verbose option causes the tool to list what is generated.)  After the files have been generated you will need to provide an implementation of the client (i.e. a class equivalent to InteropSampleClient.java).


Running the Client
==================
The client can be run via the axis2.sh or axis2.bat scripts (e.g. 'axis2.sh org.apache.axis2.jaxws.interop.InteropSampleClient'.)  The endpoint URL is extracted at runtime from the WSDL, so modify the soap:address to refer to your local endpoint. Axis2 deploys InteropSampleService as BaseDataTypesDocLitBService.BasicHttpBinding_IBaseDataTypesDocLitB, i.e. http://localhost:8080/axis2/services/BaseDataTypesDocLitBService.BasicHttpBinding_IBaseDataTypesDocLitB (the Microsoft interop endpoint that the WSDL was written against no longer exists).  
