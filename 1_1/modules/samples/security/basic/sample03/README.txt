UsernameToken authentication with a plain text password

The client is configured to add a UsernameToken to the outgoing message.
	- See the "OutflowSecurity" parameter in the client.axis2.xml
	- Note the <passwordType>PasswordText</passwordType> element
	
The service is configured to process it.
	- See the "InflowSecurity" parameter in the services.xml

Note how org.apache.rampart.samples.sample03.PWCBHandler authenticates the 
password

