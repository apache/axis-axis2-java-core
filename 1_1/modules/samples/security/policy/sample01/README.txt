UsernameToken Authentication

The policy uses a TransportBinding and requires a SignedSupportingToken which 
is a UsernameToken and the inclusion of a TimeStamp. 

Note that Rampart does not enforce the use of HTTPS transport and that 
{http://ws.apache.org/rampart/policy}RampartConfig assertion provides
additional information required to secure the message.