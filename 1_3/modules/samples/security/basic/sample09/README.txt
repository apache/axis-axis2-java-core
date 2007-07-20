Encryption with a key known to both parties

Both client and servce are configured to encrypt the outgoing message and to 
decrypt incoming message using a known named key
	- See the "OutflowSecurity" and "InflowSecurity" parameters in the 
      client.axis2.xml and serivces.xml files
    - Note the use of <EmbeddedKeyName>SessionKey</EmbeddedKeyName>
    - Note that org.apache.rampart.samples.sample09.PWCBHandler sets the key
