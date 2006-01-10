======================================================
Apache Axis2 @axis2_version@ build (@release_date@)
Standard Source Release

http://ws.apache.org/axis2
------------------------------------------------------

This is the Standard source release of Axis2.

The modules directory contains source code of the following 
Axis2 modules:

1. core
2. common
3. xml
4. wsdl
5. adb
6. codegen
7. webapp
8. doom

The lib directory contains all third party library dependencies, that are distributable,
of the above modules.

The samples directory contains all the Axis2 module & service samples.

One can use maven 1.x to create the standard binary distribution out of this, by typing "$maven dist-std-bin".

(Please note that this does not include the other WS-* implementation modules, like WS-Security, that are being developed
 within Axis2. Those can be downloaded from http://ws.apache.org/axis2/modules/)


