Axis2 JAX-WS start-from-java (also known as bottoms-up) sample.

   1. Given a simple schema, generate the JAXB artifacts using xjc.
   2. With the generated JAXB beans in hand, write the service implementation 
      and add annotations to make a Web Service.
   3. Write a Dispatch client to interact with the service
   4. Run the Dispatch client against the service implementation deployed in the Axis2 server

This will be an address book sample based on one of the jaxws-integration tests:
modules/jaxws-integration/test/org/apache/axis2/jaxws/sample/addressbook

Note that this is a very simple example, and does not persist any data.  The intent is
to illustrate the use of JAXB objects with JAX-WS, not to actually implement and address book.

The directory structure and environment setup in this example is as follows.  You should modify
the example directories based on your environment.
- Axis2 binary distribution: AXIS2_HOME=C:\temp\Axis2\axis2-SNAPSHOT
- Java5 JDK: JAVA_HOME=c:\java\java5 
- This example source is in: C:\blddir\eclipse\axis2\jaxws-bottoms-up 

Note that JAVA_HOME is assumed to be on PATH.

The following source is included with this example:
src\AddressBookEntry.xsd:  Schema used to generate JAXB artifacts
src\org\apache\axis2\jaxws\addressbook\AddressBook.java:  JAXWS Service Endpoint Interface (SEI)
    Note that this SEI is NOT CURRENTLY USED in this example.
src\org\apache\axis2\jaxws\addressbook\AddressBookClient.java: JAXWS Dispatch Client
src\org\apache\axis2\jaxws\addressbook\AddressBookImpl.java: JAXWS service implementation

Note that this ReadMe was created with the example in an Eclipse project with the following
structure:
- 'src' source folder containing the example source
- 'jaxb' source folder containing the generated JAXB artifacts from Step 1
- 'bin' directory containing all of the output (including the compiled classes)

Step 1: Generate JAXB artifacts from simple schema
==================================================
The file src/AddressBookEntry.xsd describes a simple AddressBookEntry object with the
following fields:
    String firstName;
    String lastName;
    String phone;
    String street;
    String city;
    String state;

To generate the JAXB beans for this schema, run the following command in the 'src' directory:
java -Djava.ext.dirs=C:\temp\Axis2\axis2-SNAPSHOT\lib;C:\java\java5\jre\lib\ext com.sun.tools.xjc.Driver -d jaxb src\AddressBookEntry.xsd

This will generate the following JAXB artifacts:
org\apache\axis2\jaxws\addressbook\AddressBookEntry.java
org\apache\axis2\jaxws\addressbook\ObjectFactory.java
org\apache\axis2\jaxws\addressbook\package-info.java

You can compile these files now or later in Step 3 with the rest of the java files.  See Step 3
for the 'javac' command. 

Step 2: Write a JAX-WS service implementation using the JAXB artifacts
======================================================================
The simple service implementation will have two methods on it:
    public String addEntry(String firstName, String lastName, String phone, String street, String city, String state)
    public AddressBookEntry findByLastName(String lastName)
    
The service implentation does not explicitly specify a JAX-WS SEI.  The public methods on the
implementation are an implicit SEI.  Simply by adding an @WebService annotation to the implementation
it becomes a JAX-WS web service.   

See src\org\apache\axis2\jaxws\addressbook\AddressBookImpl.java  

You can compile these files now or later in Step 3 with the rest of the java files.  See Step 3
for the 'javac' command. 

Step 3: Write a JAX-WS Dispatch client to interact with the service
===================================================================
The extremely simple Dispatch client will use be a Payload mode String Dispatch client meaning that
it will provide the exact SOAP body to send in the request (Payload mode) as a String, and expect 
the response to be a SOAP body returned as a String.  It will invoke both methods on the  
service implenetation's implicit SEI.

Step 4: Run the Dispatch client against the service implementation deployed in the Axis2 server
===============================================================================================
(a) First compile the generated JAXB artifacts, the service implementation, and the dispatch client.
Note that if you are using Eclipse, this step will not be necessary; Eclipse will have compiled the
classes into the 'bin' directory.
javac -Djava.ext.dirs=C:\temp\Axis2\axis2-SNAPSHOT\lib;C:\java\java5\jre\lib\ext -classpath C:\blddir\eclipse\axis2\jaxws-bottoms-up\bin *.java

(b) Then create a JAR file containg the service implementation and copy it to 
the axis2 repository/servicejars directory.  This will cause it to be deployed when the axis2 
server is started.  Note that in this example, the service implementation jar will also contain 
the client classes; this is not necessary; it is done to simplify the example. From the directory
above 'org' which contains the compiled classes from (a):
jar -cvf AddressBook.jar org
mkdir C:\temp\Axis2\axis2-SNAPSHOT\repository\servicejars
copy AddressBook.jar C:\temp\Axis2\axis2-SNAPSHOT\repository\servicejars

(c) Start the axis2 server.  This will deploy the JAX-WS service implementation.
set AXIS2_HOME=C:\temp\Axis2\axis2-SNAPSHOT
bin\axis2server.bat

You should see a message such as:
[INFO] Deploying artifact : AddressBook.jar
[INFO] Deploying JAXWS annotated class org.apache.axis2.jaxws.addressbook.AddressBookImpl as a service - AddressBookImplService.AddressBookImplPort

(d) From another window with the environment setup, run the Dispatch client:
java -Djava.ext.dirs=C:\temp\Axis2\axis2-SNAPSHOT\lib;C:\java\java5\jre\lib\ext -cp C:\blddir\eclipse\axis2\jaxws-bottoms-up\bin org.apache.axis2.jaxws.addressbook.AddressBookClient.class 

Thoughts on improvmenets
========================
1. Extend the simple schema to include the request and response messages, generate those beans
   with xjc, and then use them in the Dispatch client instead of the String messages

2. Make use of the JAXWS SEI by specifying an @WebService annotation on it and
   and @WebService.endpointInterface on the serivice implementation