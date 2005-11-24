Hey! this test works only if on the server side the version.aar provided
in our test-resources directory is deployed. That in turn would need us 
to make available org.apache.axis.jaxrpc.server.JAXRPCInOutMessageReceiver
class file to the server class loader. Because, in that version.aar we coded
in the service.xml to pick this message receiver.