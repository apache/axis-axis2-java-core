
This is an example POJO web service. It shows how to expose the methods of a Java class as a web service using Aixs2. 

To build the sample service you must have ant-1.6.x installed in your system. 

To set AXIS2_HOME in Unix/Linux type:
$export AXIS2_HOME=<path to axis2 distribution>

To build the sample service, type:
$ant 

This will build the AddressBookService.aar in the build directory and copy it to the <AXIS2_HOME>/repository/services directory.

The WSDL for this service should be viewable at:
http://<yourhost>:<yourport>/axis2/services/AdressBookService?wsdl 

src/sample/addressbook/rpcclient/AddressBookRPCClient.java is a Client that uses RPCServiceClient to invoke the methods of this web services just like the method invocations of a Java object.

To compile and run, type
$ant rpc.client

src/sample/addressbook/adbclient/AddressBookADBClient is Client that uses a generated stub with ADB to invoke the methods of this web service.

To generate the stub, compile and run, type
$ant adb.client -Dwsdl=http://<yourhost>:<yourport>/axis2/services/AdressBookService?wsdl




