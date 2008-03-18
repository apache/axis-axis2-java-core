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
6. distribution
7. fastinfoset
8. integration
9. java2wsdl
10. jaxbri
11. jaxws
12. jaxws-api
13. jibx
14. json
15. jws-api
16. kernel
17. metadata
18. mex
19. mtompolicy
20. parent
21. ping
22. saaj
23. saaj-api
24. samples
25. scripting
26. soapmonitor
27. spring
28. tool
29. webapp
30. xmlbeans

One can use maven 2.x to create the Standard Binary Distribution from this, 
by typing "mvn clean install".

(Please note that this does not include the other WS-* implementation modules, 
like WS-Security, that are being developed within Axis2. Those can be downloaded
 from http://ws.apache.org/axis2/modules/)
 


