/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
======================================================
Apache Axis2 @axisVersion@ build (@TODAY@)
Source Release

http://ws.apache.org/axis2
------------------------------------------------------

This is the Standard Source Release of Axis2.

The modules directory contains source code of the following 
Axis2 modules:

1. adb
2. adb-codegen
3. addressing
4. clustering
5. codegen
6. corba
7. distribution
8. documentation
9. fastinfoset
10. integration
11. java2wsdl
12. jaxbri
13. jaxws
14. jaxws-api
15. jaxws-integration
16. jibx
17. json
18. jws-api
19. kernel
20. metadata
21. mex
22. mtompolicy
23. parent
24. ping
25. saaj
26. saaj-api
27. samples
28. scripting
29. soapmonitor
30. spring
31. tool
32. webapp
33. xmlbeans

One can use maven 2.x to create the Standard Binary Distribution from this, 
by typing "mvn clean install".

(Please note that this does not include the other WS-* implementation modules, 
like WS-Security, that are being developed within Axis2. Those can be downloaded
 from http://ws.apache.org/axis2/modules/)
 


