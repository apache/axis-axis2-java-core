
This example demonstrates the use of asynchronous web method invocations. 

In this sample, the use can continue to type text on the text editor where the text editor does the spell check from a hosted web service. The web method invocation is done in as an asynchronous web method invocation allowing the user to input text continuously. 

Note that when running the build script, stub is generated for the hosted spell check service at: http://tools.wso2.net:12001/axis2/services/SimplifiedSpellCheck?wsdl

and the text editor use that stub and the callback handler to provided the required functionality.

To generate stubs and run the text editor, type
$ant




