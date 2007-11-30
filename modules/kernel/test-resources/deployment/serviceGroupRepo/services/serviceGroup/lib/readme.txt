The testlib.jar file in here contains a single class, "TestClass".  This class
specifically does NOT live anywhere in the compilable source for Axis2, because
there is a test to see if we can load the class in ServiceGroupTest and we want
to make sure this jar is the only place it could have come from.  The jar contains
the source file in case you want to look at it.

This part of the test written by Glen Daniels (gdaniels@apache.org), ask me or
axis-dev@ws.apache.org if you have questions.
