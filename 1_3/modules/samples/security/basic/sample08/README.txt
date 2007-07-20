Signing twice

The client is configured to sign the outgoing message twice
	- See the "OutflowSecurity" parameter in the client.axis2.xml
	- Note the aditional <action> element that defines the second signature.
	
The service is configured to process it.
	- See the "InflowSecurity" parameter in the services.xml. Not that we 
      simply use "Signature Signature" as action items.

