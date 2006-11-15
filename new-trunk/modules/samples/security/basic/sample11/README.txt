Dynamic configuration : Get rid of the config files ... let's use code!

Both client and servce are configured to first sign and then encrypt the 
outgoing message and to decrypt and verify the incoming message using their 
key pairs.
	- Note that we don't use any parameters in the client.axis2.xml
    - See org.apache.rampart.samples.sample11.Client's getOutflowConfiguration()
      getInflowConfiguration() methods and their usage.
