********************************************************************************
**************************** Apache Rampart Samples ****************************
********************************************************************************

This directory contains three sub directories:

    - basic - A set of samples that uses basic rampart configuration using 
    	          parameters

    - policy - A set of samples that uses rampart with WS-SecurityPolicy

    - rahas - A set of samples demonstrating WS-Trust features provided by Rahas

IMPORTANT: Before you try any of the samples makesure you

1.) Have the Axis2 standard binary distribution downloaded and extracted. 
2.) Set the AXIS2_HOME envirenment variable
3.) Run ant from the "samples" directory to copy the required libraries and 
    modules to relevant directories in AXIS2_HOME.
4.) Download xalan-2.7.0.jar from here[1] and put under AXIS2_HOME\lib folder,
    if you use JDK 1.5. 

[1] http://www.apache.org/dist/java-repository/xalan/jars/