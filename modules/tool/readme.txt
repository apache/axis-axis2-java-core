================
Using the tools  
================

(1) elcipse plugins

* Since the source for the tools has a dependency on the eclipse classes. one has to run the
  ant build file (create-project.xml) to generate a relevant eclipse project from the source.

* [use the ant -f create-project.xml command]

* Once the projects are generated (which can be found in the newly created eclipse_project 
  directory) they can be opened in the Eclipse PDE for building and editing.

* If you need to build and install the eclipse plugin to your local eclipse plugin directory
	* Edit the eclipse.home property to your eclipse home directory.
	* stop eclpse if still on operation.
	* use ant -f create-project.xml install-codegen-plugin install-service-plugin
        * or to release the plugins [ant -f create-project.xml release-plugins]
	* start eclipse
	* plugins will be accessible through [File -> New -> Other] ctl+n 

* The tool sources are not included in the build

Note - The plugins are specifially for Eclipse version 3.1 and up
