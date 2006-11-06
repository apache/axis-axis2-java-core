Sample: Service Life Cycle
==========================

Introduction
============

This sample is to demonstrate usage of a service life cycle interface and session scoping with a
sample on application scope session.  The sample consists of two parts;
   Service and Client

Let's take a library system, which has the following operations, as the scenario for this sample.

 - Register user
 - Login to system
 - View all books
 - View available books
 - View lend books
 - Lend and return books


Why Do We Need Service Life Cycle Here?
=======================================

In order to make sure that at the runtime service impl class can access data read from anywhere, 
we need to initialize the service at deployment. Let's forget about the session for a minute
and think about the scenario where you want to populate a bean or need to start a Data Base connection for the
service. In such a situation having service life cycle class is very useful.

In this sample you will learn how to use the service life cycle class for your application. The
interface has only two methods
1. startUp
2. shutDown

Where startUp method will be called when the service is deployed and it will call only once for that
particular service, while shutDown method calls when system shutdown.

So in this sample, the life cycle class will load the library inside the startUp method. At the
very first time the system will load from services.xml. It has all the available books in the
system. And at the time of system shutdown it will save the status of the library into a file
(library.xml) in the java.io.temp directory. So the next time onwards it will populate library
using the above file. So all the changes will be there in the new instance as well.


How to Build the Service?
==========================

Building the service is just a matter of running ant build file. It will create the service aar
file, or you could just run the copy.to.tomcat goal which will copy the aar file into
tomcat/web-app/axis2/WEB-INF/services directory.

Next, go to  http://[url and port to your app server]/axis2 and see whether your service is listed
there.

Running the Client
==================

You  can invoke complete service using REST API, but it only works in application servers like Apache Tomcat and it will not work in SimpleHttpServer. First try to use library system using REST API.

This sample is a simple one and will not do complex tasks. So let's follow steps given below to make
 sure that the sample works as it should:
 
Step 1:- View all the available books in the library, just type the following URL on your favorite
browser.
http://127.0.0.1:8080/axis2/rest/Library/listAvailableBook

You will see all the available books in the system

Step 2 : View all the books in the system 
http://127.0.0.1:8080/axis2/rest/Library/listAllBook

In this case you will get exactly the same result for both the request, since no one has borrowed
the book.

Step 3: Now look at all the lent books in the system. And you will see an empty list at
http://127.0.0.1:8080/axis2/rest/Library/listLendBook

Step 4 : Register a user. To lend a book or return a book, you need to register first. Do not register twice, 
if you try to do so you will get an exception.

To register you need to give user name and password (have a look at the wsdl at http://127.0.0.1:port/axis2/services/serviceName?wsdl. You will see what you
 need to pass in)

So let's create a user called 'foo' with the password '123' 
http://127.0.0.1:8080/axis2/rest/Library/register?userName=foo&passWord=123

Then you will get the following response, if everything has gone well.
<ns:registerResponse>
     <ns:return>true</ns:return>
</ns:registerResponse>

Step 5 : Log into system. To log in you need to pass user name and password. So let's try to login
to system.
http://127.0.0.1:8080/axis2/rest/Library/login?userName=foo&passWord=123

Step 6  : Now we are ready to lend a book. To lend a book you need to pass isbn
and userName. Let's try to lend the book with isbn  0-937175-77-3, your request would look like
below:

http://127.0.0.1:8080/axis2/rest/Library/lendBook?isbn=0-937175-77-3&userName=foo

If everything went well, you will see the following response in the browser.

<ns:lendBookResponse>
	<ns:return>
               <author>John Bloomer</author>
                <isbn>0-937175-77-3</isbn>
                <title>Power Programming with RPC</title>
          </ns:return>
</ns:lendBookResponse> 

Step 7 : Now go and look at lent list again at http://127.0.0.1:8080/axis2/rest/Library/listLendBook
you will see the following output in the browser.

<ns:listLendBookResponse>
	<ns:return>
               <author>John Bloomer</author>
               <isbn>0-937175-77-3</isbn>
               <title>Power Programming with RPC</title>
          </ns:return>
</ns:listLendBookResponse>

That's the book you just lent! 

Note : Now if we view the available books, you will not see the above book in that list.

Step 8 : Now let's return the book.
http://127.0.0.1:8080/axis2/rest/Library/returnBook?isbn= 0-937175-77-3 

Step 8 : Now let's look at the lent book list again, and you will realize that list is empty.

Step 9 : Lend few more book and re-start the app server (say Tomcat), and see whether all the previous data
is there in the new instance.

Running Java Client
===================
Open up the LibraryServiceClient java class in your favorite IDE and run the main class. And you can
 comment and un-comment whatever the method you want to invoke.


Running Java client Using Ant
=============================
invoke
ant run.client

And then follow the instructions as mentioned in the console.
