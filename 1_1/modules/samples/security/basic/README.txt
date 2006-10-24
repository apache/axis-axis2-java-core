This is a set of Apache Rampart samples which uses configuraiton parameters 
to configure rampart.

Each "sample" directory contains :

    - client.axis2.xml - Client configuration
    - services.xml - Service configuration

We use two parameters named "InflowSecurity" and "OutflowSecurity" within
these files to configure rampart.

01.) Rampart Engaged and no configuration
02.) UsernameToken authentication
03.) UsernameToken authentication with plain text password
04.) Message integrity and non-repudiation with signature
05.) Encryption
06.) Sign and encrypt a messages
07.) Encrypt and sign messages
08.) Signing twice
09.) Encryption with a key known to both parties
10.) MTOM Optimizing base64 content in the secured message
11.) Dynamic configuration : Get rid of the config files ... let's use code!
