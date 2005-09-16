This sample provide the capabilities and power of MTOM support over AXIOM.
In this sample a .jpg image is send to a service, which will eventually save
it according to the location that the user providing.

Sample runs as follows.

1.  Use MTOMClient to run the Sample.

2.  Brows for .jpg image to transfer

3.  Give the location to save the sample.

4.  Before running sample, service should be present
{$tomcat_home}/webapps/axis2/WEB-INF/services. In order to create the .aar file
use the Service “MTOMService” which is present in the
“sample” module's sample/mtom/ folder. Create the folder structure as mentioned
in the Axis2-user guide.
 
5.  Run the sample and if everything goes well a message will be popped up
with conformation.