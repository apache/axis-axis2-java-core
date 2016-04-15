<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

Axis2 Reference Guide
=====================

WSDL2Java Reference
-------------------

    NAME
           wsdl2java.sh or wsdl2java.bat - Generates java code according to a given WSDL file to handle Web service invocation.
           These scripts can be found under the bin directory of the Axis2 distribution.
    
    SYNOPSIS
           wsdl2java.sh [OPTION]... -uri <Location of WSDL>
    
    DESCRIPTION
           Given a WSDL file, this generates java code to handle Web service invocations.
    
          -o <path>                Specify a directory path for the generated code.
          -a                       Generate async style code only (Default: off).
          -s                       Generate sync style code only (Default: off). Takes precedence over -a.
          -p <pkg1>                Specify a custom package name for the generated code.
          -l <language>            Valid languages are java and c (Default: java).
          -t                       Generate a test case for the generated code.
          -ss                      Generate server side code (i.e. skeletons) (Default: off).
          -sd                      Generate service descriptor (i.e. services.xml). (Default: off). Valid with -ss.
          -d <databinding>         Valid databinding(s) are adb, xmlbeans, jibx and jaxbri (Default: adb).
          -g                       Generates all the classes. Valid only with -ss.
          -pn <port_name>          Choose a specific port when there are multiple ports in the wsdl.
          -sn <service_name>       Choose a specific service when there are multiple services in the wsdl.
          -u                       Unpacks the databinding classes
          -r <path>                Specify a repository against which code is generated.
          -ns2p ns1=pkg1,ns2=pkg2  Specify a custom package name for each namespace specified in the wsdls schema.
          -ssi                     Generate an interface for the service implementation (Default: off).
          -wv <version>            WSDL Version. Valid Options : 2, 2.0, 1.1
          -S                       Specify a directory path for generated source
          -R                       Specify a directory path for generated resources
          -em                      Specify an external mapping file
          -f                       Flattens the generated files
          -uw                      Switch on un-wrapping.
          -xsdconfig <file path>   Use XMLBeans .xsdconfig file. Valid only with -d xmlbeans.
          -ap                      Generate code for all ports
          -or                      Overwrite the existing classes
          -b                       Generate Axis 1.x backword compatible code.
          -sp                      Suppress namespace prefixes (Optimzation that reduces size of soap request/response)
          -E<key> <value>          Extra configuration options specific to certain databindings. Examples:
                                   -Ebindingfile <path>                   (for jibx) - specify the file path for the binding file
                                   -Etypesystemname <my_type_system_name> (for xmlbeans) - override the randomly generated type system name
                                   -Ejavaversion 1.5                      (for xmlbeans) - generates Java 1.5 code (typed lists instead of arrays)
                                   -Emp <package name> (for ADB) - extension mapper package name
                                   -Eosv (for ADB) - turn off strict validation.
                                   -Eiu (for ADB) - Ignore Unexpected elements instead of throwing ADBException
                                   -Ewdc (for xmlbeans) - Generate code with a dummy schema. if someone use this option
                                      they have to generate the xmlbeans code seperately with the scomp command comes with the
                                      xmlbeans distribution and replace the Axis2 generated classes with correct classes
          --noBuildXML             Dont generate the build.xml in the output directory
          --noWSDL                 Dont generate WSDLs in the resources directory
          --noMessageReceiver      Dont generate a MessageReceiver in the generated sources
          --http-proxy-host        Proxy host address if you are behind a firewall
          --http-proxy-port        Proxy prot address if you are behind a firewall
          -ep                      Exclude packages - these packages are deleted after codegeneration
        
    EXAMPLES
           wsdl2java.sh -uri ../samples/wsdl/Axis2SampleDocLit.wsdl
           wsdl2java.sh -uri ../samples/wsdl/Axis2SampleDocLit.wsdl -ss -sd 
           wsdl2java.sh -uri ../samples/wsdl/Axis2SampleDocLit.wsdl -ss -sd -d xmlbeans -o ../samples -p org.apache.axis2.userguide

Java2WSDL Reference
-------------------

    NAME
           Java2WSDL.sh or Java2WSDL.bat - Generates the appropriate WSDL file for a given java class.
           These scripts can be found under the bin directory of the Axis2 distribution.
    
    SYNOPSIS
           Java2WSDL.sh [OPTION]... -cn <fully qualified class name>
    
    DESCRIPTION
           Given a java class generates a WSDL file for the given java class. 
    
          -o <output location>                    output directory
          -of <output file name>                  output file name for the WSDL
          -sn <service name>                      service name
          -l <one or more soap addresses>         location URIs, comma-delimited
          -cp <class path uri>                    list of classpath entries - (urls)
          -tn <target namespace>                  target namespace for service
          -tp <target namespace prefix>           target namespace prefix for service
          -stn <schema target namespace>          target namespace for schema
          -stp <schema target namespace prefix>   target namespace prefix for schema
          -st <binding style>                     style for the WSDL
          -u <binding use>                        use for the WSDL
          -nsg <class name>                       fully qualified name of a class that implements NamespaceGenerator
          -sg <class name>                        fully qualified name of a class that implements SchemaGenerator
          -p2n [<java package>,<namespace>] [<java package>,<namespace>]
                                                  java package to namespace mapping for argument and return types
          -p2n [all, <namespace>]                 to assign all types to a single namespace
          -efd <qualified/unqualified>            setting for elementFormDefault (defaults to qualified)
          -afd <qualified/unqualified>            setting for attributeFormDefault (defaults to qualified)
          -xc class1 -xc class2...                extra class(es) for which schematype must be generated.
          -wv <1.1/2.0>                           wsdl version - defaults to 1.1 if not specified
          -dlb                                    generate schemas conforming to doc/lit/bare style
          -dne                                    disallow nillable elements in the generated schema
          -doe                                    disallow optional elements in the generated schema
          -disableSOAP11                          disable binding generation for SOAP 1.1
          -disableSOAP12                          disable binding generation for SOAP 1.2
          -disableREST                            disable binding generation for REST
        
    EXAMPLES
           Java2WSDL.sh -cn ../samples/test/searchTool.Search
           Java2WSDL.sh -cn ../samples/test/searchTool.Search -sn search
           Java2WSDL.sh -cn ../samples/test/searchTool.Search -u -sn search
           Java2WSDL.sh -cn ../samples/test/searchTool.Search -sn search -o ../samples/test/wsdl  
