================
Using the tools  
================

(1) elcipse plugins

Note - The plugins are specificallyqp for Eclipse version 3.1 and up

Create Eclipse Plugin Projects
------------------------------

	* Since the source for the tools has a dependency on the eclipse classes. one has to run the
	  ant build file (create-project.xml) to generate a relevant eclipse project from the source.
	  
	* In order to compile the plugin first you must do a maven create-lib on Axis2 Source and
	  set ECLIPSE_HOME environment variable to point to your eclipse home directory.  
	
	* use the ant -f create-project.xml command to generate the plugin projects.
	
	* Once the projects are generated (which can be found in the newly created eclipse_project 
	  directory) they can be opened in the Eclipse PDE for building and editing.
	  
	* This can be done by File -> Import -> Existing project into workspace on Elcipse menu and 
	  point that to the eclipse_project directory newly created.

Build Eclipse Plugin Projects
------------------------------

	* If you need to build and install the eclipse plugin to your local eclipse plugin directory
		* In order to compile the plugin first you must do a maven create-lib on Axis2 Source and
	  	  set ECLIPSE_HOME environment variable to point to your eclipse home directory
		* stop eclpse if still on operation.
		* use ant -f create-project.xml install-codegen-plugin install-service-plugin
	      or to release the plugins [ant -f create-project.xml release-plugins]
		* start eclipse
		* plugins will be accessible through [File -> New -> Other] ctl+n under Axis2 Wizards.
	
	* The tool sources are not included in the build
	
	* To run the plugin you need please refer to 
		- Tools Page On Apache Axis 2 Documentation  http://ws.apache.org/axis2/tools/
