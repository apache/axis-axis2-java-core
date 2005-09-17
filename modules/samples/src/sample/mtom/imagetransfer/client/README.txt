Sample for MTOM
===============

Overview:
---------

This sample provide the capabilities and power of MTOM support over AXIOM.
In this sample a .jpg image is send to a service, which will eventually save
it according to the location that the user providing.

PreConditions:
--------------

1.  Run maven dist-bin on project root. The generated target/dist folder contains
the binary distribution. Unzip the relevant distribution and i would name the
location as AXIS2_DIST.

2.  mtomSample.aar, which is the service required for this sample can be found
from AXIS2_DIST/samples/mtom folder. Drop it down in Tomcat's Axis2's
services directory.[{$tomcat_home}/webapps/axis2/WEB-INF/services]

Running the Sample:
-------------------

Use <ant mtomSample> command to run the sample. bulid.xml is available at
AXIS2_DIST/samples folder. Second way is the scrip file. Use run.sh or run.bat
to run the sample pertaining to the system you are using.


UI Configuration:
-----------------
1.  First brows the .jpg picture that you have to transmit.

2.  Give the desired location that you have to save it. for example [/usr/temp/temp.jpg]
If the everything work fine, you will get the conformation or an exception
via a Dialog box.  End point reference is given. You can change it according to
you host.

Please contact axis-dve list if you have found any trouble running the sample

