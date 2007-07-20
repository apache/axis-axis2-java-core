Sign and encrypt messages

Both client and servce are configured to first sign and then encrypt the 
outgoing message and to decrypt and verify the incoming message using their 
key pairs.
	- See the "OutflowSecurity" and "InflowSecurity" parameters in the 
      client.axis2.xml and serivces.xml files
    - Note the use of <optimizeParts>[xpath expression]</optimizeParts>
