Sample for MTOM
===============

Overview:
---------

This sample demonstrates the capabilities and power of MTOM & SwA support of 
AXIS2. In this sample the user can send multiple files to the MTOM sample 
service, which will save them on the server or echo them depending on the 
selected operation (i.e. send or send & receive). Echoing would result the files
to be saved on the client side.

The option of client side file caching can be used when echoing a larger amount 
of data which could result in memory overflows. 



Running the Sample:
-------------------
1. Use <ant service> command in the AXIS2_DIST/sample/mtomsample/ to build the service.
2. Drop the mtomSample.aar service to the AXIS2_DIST/repository/services.
3. run the AXIS2_DIST/bin/axis2server.{sh.bat} to start the standalone axis2 server. 
(Alternatively you can drop the sample to the services directory of a Axis2 server
running in a servlet container)
4. Use <ant client> to build and run the client.


UI Configuration:
-----------------
1.  First browse a file or type the absolute path of a file that you want to 
transmit. Then click "Add".  Add any number of files as you like.

2.  Give the desired location that you want the files to be saved, for example 
[/usr/temp/]. In the upload scenario(send) it must be a directory in the machine 
which runs the Axis2 server. In echo scenario(send receive) it must be a 
directory in the client machine.

3. The default end point reference is given, assuming you are deploying the 
mtomSample service in the same machine. You can change it according to your host.

3.  Select the transfer method either as MTOM or as SOAP with attachments. 
Select the operation as either to send or send & receive.

4.  Client side File caching is enabled only in the send & receive operation. 
Enter a threshold value in bytes so that files larger than this will be cached. 
Files will be cached in the path given in the cache folder text box. This is 
a must if you are planning to echo large attachments. 

5.  Pressing "Execute" will perform the intended operation under given 
configurations. On success a message box will appear informing the number of 
files saved.


Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble 
running the sample.

