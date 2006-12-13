SecureConversation 

The secure session is bootstrapped using a SymetricBinding which uses
derived keys based on an ephemeral key. 

Messages in the secure conversation :
	- Includes a timestamp
	- All headers are signed along with the timestamp
	- Signature encrypted
	- Body encrypted

Algorithm suite is Basic128Rsa15

Note that {http://ws.apache.org/rampart/policy}RampartConfig assertion provides
additional information required to secure the message.