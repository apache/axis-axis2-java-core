/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis.deployment;

/**
 * DeployCons interface is to keep constent value required for Deployemnt
 */
public interface DeploymentConstants {
    int SERVICE = 0; // if it is a servise
    int MODULE = 1; // if it is a module

    String SERVICEXML = "META-INF/service.xml";
    String SERVICEWSDL ="META-INF/service.wsdl";
    String MODULEXML = "META-INF/module.xml";
    String PARAMETERST = "parameter";// paramater start tag
    String HANDERST = "handler";
    String MODULEST = "module";
    String PHASEST = "phase";
    String PHASE_ORDER = "phaseOrder";
    String TYPEMAPPINGST = "typeMapping";// typeMapping start tag
    String BEANMAPPINGST = "beanMapping";// beanMapping start tag
    String OPRATIONST = "operation";// operation start tag
    String INFLOWST = "inflow";// inflow start tag
    String OUTFLOWST = "outflow";// outflowr start tag
    String IN_FAILTFLOW = "INfaultflow";// faultflow start tag
    String OUT_FAILTFLOW = "Outfaultflow";// faultflow start tag

    // String FOLDE_NAME = "D:/Axis 2.0/projects/Deployement/test-data";
    String MODULE_PATH = "/modules/";
    String SERVICE_PATH = "/services/";

    // for jws file extension
    String JWS_EXTENSION = ".jws";

    String SERVICETAG = "service";
    String SERVERST = "server";

    // for serviemetadat
    String PROVIDERNAME = "provider";
    String STYLENAME = "style";
    String CONTEXTPATHNAME = "contextPath";

    // for parameters
    String ATTNAME = "name";
    String ATTLOCKED = "locked";
    String TYPE = "type";

    // for operatiins
    String ATQNAME = "name";
    String ATUSE = "use";
    String MEP = "mep";

    //for handlers
    String REF = "ref";
    String CLASSNAME = "class";
    String BEFORE = "before";
    String AFTER = "after";
    String PHASE = "phase";
    String PHASEFIRST = "phaseFirst";
    String PHASELAST = "phaseLast";
    String ORDER = "order";  // to resolve the order tag

    String JAVAST = "http://ws.apache.org/axis2/deployment/java";
    String JAVAIMPL = "implementation";

    String TRANSPORTSTAG = "transports";
    String TRANSPORTTAG = "transport";
    String TRANSPORTSENDER = "transportSender";
    String TRANSPORTRECEIVER = "transportReceiver";

    String MESSAGERECEIVER = "messageReceiver";


    String HOTDEPLOYMENT = "hotdeployment";
    String HOTUPDATE = "hotupdate";

}
