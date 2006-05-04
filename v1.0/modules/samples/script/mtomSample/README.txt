Sample for MTOM
===============

Overview:
---------

This sample provides the capabilities and power of MTOM support of AXIOM.
In this sample a jpg image is send to a service, which will eventually save
it according to the location the user provided.

PreConditions:
--------------

1.  Run maven dist-bin on project root. The generated target/dist folder contains
the binary distribution. Unzip the relevant distribution to a folder of choice (this
will be referred to as AXIS2_DIST)

2.  mtomSample.aar, which is the service required for this sample can be found
from AXIS2_DIST/samples/mtom folder. Drop it down in Tomcat's Axis2's
services directory.[{$tomcat_home}/webapps/axis2/WEB-INF/services]

Running the Sample:
-------------------

1. Use <ant mtomSample> command to run the sample. bulid.xml is available at
AXIS2_DIST/samples folder
2. Second way is the script file. Use run.sh or run.bat to run the sample
pertaining to the system you are using.


UI Configuration:
-----------------
1.  First browse the .jpg picture that you have to transmit.

2.  Give the desired location that you have to save it. for example [/usr/temp/temp.jpg]
If everything works fine, you will get the conformation or an exception
via a dialog box.  End point reference is given. You can change it according to
your host.

Please contact axis-dev list (axis-dev@ws.apache.org) if you have any trouble running the sample.

