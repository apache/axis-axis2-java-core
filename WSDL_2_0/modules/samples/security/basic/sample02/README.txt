UsernameToken authentication

The client is configured to add a UsernameToken to the outgoing message.
	- See the "OutflowSecurity" parameter in the client.axis2.xml
	
The service is configured to process it.
	- See the "InflowSecurity" parameter in the services.xml

Note how org.apache.rampart.samples.sample02.PWCBHandler supplies the password 
to wss4j to compute the digest for comparison.
