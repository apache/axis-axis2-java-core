This sample is not included in the binary distribution. To run it, copy this directory
into AXIS2_HOME/modules/samples/ of an extracted Axis2 binary distribution and run ant
from there (the build resolves AXIS2_HOME as ../../..).
Apache Ant 1.10 or later is required.

To run this sample follow theses steps
1. run generate_service ant task. (ant generate_service)
   this generates the service and put it to the rmiservice folder under repository
2. start the axis2 server 
3. run the client (ant run_client)
