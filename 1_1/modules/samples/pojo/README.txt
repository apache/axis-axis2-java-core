
This is an example POJO web service. It shows how to expose the methods of a Java class as a web service using Aixs2. 

To build the sample service you must have set the AXIS2_HOME environment variable and you must have ant-1.6.x installed in your system. 

To set AXIS2_HOME in Unix/Linux type:
$export AXIS2_HOME=<path to axis2 distribution>

To build the sample service, type:
$ant 

This should build a AdressBookService.aar that can be deployed as a Axis2 web service.

To deploy the  AdressBookService.aar, copy it to  <AXIS2_HOME>/repository/services directory and restart the server.

The WSDL for this service should be viewable at:
http://<yourhost>:<yourport>/axis2/services/AdressBookService?wsdl 

You can run the address_book_client/AddressBookClient.java that uses RPCServiceClient to invoke  methods of this web service just like method invocations of Java Objects.



