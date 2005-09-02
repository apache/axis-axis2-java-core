The testcase JUnitTestAttachmentsIntegration.java is a round trip 
integration test written to put to test the attachment support added 
to SAAJ module. However this test can't be run as part of maven b'coz
of following pre-requisites
A web service to echo back attachments is expected to be up and running
at "http://localhost:8080/axis2/services/Echo" with operation name as
"echo". We don't want the maven to break because of this dependency.
The name of the test case is hence modified not to match *Test.java pattern,
so that maven wouldn't run it.

However, for those who want to test this round trip test of attachments
support, we are providing the Echo.aar service in the test-resources
folder. Hot deploy this service on a servlet container at 8080 port and
manually run this test case.

****