Sample for MTOM
===============

Overview:
---------

This sample provides the capabilities and power of MTOM support of AXIOM.
In this sample the user can send multiple files to the MTOM sample service, which will save them
on the server or echo them depending on the selected operation (i.e. send or send & receive). Echoing would
result the files to be saved on the client side. 

The option of file caching can be used when echoing a larger amount amount of data which could result in
memory overflows. This will first save the echoed data in the local machine and then process them avoiding 
too much memory usage.

PreConditions:
--------------

1.  Run maven dist-std-bin on project root. The generated target/dist folder contains
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
1.  First browse a file or type the absolute path of a file that you want to transmit. Then click "Add".
    Add more files as you needed. 

2.  Give the desired location that you want the files to be saved, for example [/usr/temp/].
    The end point reference is given. You can change it according to your host.

3.  Select the transfer method either as via MTOM or SOAP with attachments. Select the operatio as either to
    send or send & receive.

4.  File caching is enabled only for send & receive operation. Enter a threshold value in bytes so that files larger
    than this will be cached. Files will be cached in the path given in the cache folder text box.

5.  Pressing "Execute" will perform the intended operation under given conditions. On success a message box will appear
    informing the number of files saved.
    

Please contact axis-dev list (axis-dev@ws.apache.org) if you have any trouble running the sample.

