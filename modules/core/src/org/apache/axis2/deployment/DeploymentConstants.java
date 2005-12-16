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


package org.apache.axis2.deployment;

/**
 * DeployCons interface is to keep constent value required for Deployemnt
 */
public interface DeploymentConstants {
    public static String META_INF = "META-INF";
    public static final String SERVICE_WSDL_NAME = "service.wsdl";
    int SERVICE = 0;                // if it is a service
    int MODULE = 1;                // if it is a module
    String SERVICEXML = "META-INF/services.xml";
    String SERVICE_WSDL_WITH_FOLDER = "META-INF/service.wsdl";
    String PHASE_ORDER = "phaseOrder";
    String PHASEST = "phase";
    String PARAMETER = "parameter";      // paramater start tag
    String MODULEXML = "META-INF/module.xml";
    String MODULEST = "module";
    String MODULECONFIG = "moduleConfig";
    String MESSGES = "message";
    String LISTENERST = "listener";       // paramater start tag
    String LABEL = "label";
    String HOST_CONFIG = "hostConfiguration";
    String HANDERST = "handler";
    String TYPEMAPPINGST = "typeMapping";    // typeMapping start tag
    String TYPE = "type";
    String TRANSPORTTAG = "transport";
    String TRANSPORTSTAG = "transports";
    String TRANSPORTSENDER = "transportSender";
    String TRANSPORTRECEIVER = "transportReceiver";

    // for servicemetadata
    String STYLENAME = "style";
    String SERVICE_PATH = "/services/";
    String SERVICE_GROUP_ELEMENT = "serviceGroup";

    // element in a services.xml
    String SERVICE_ELEMENT = "service";
    String SERVICETAG = "service";

    // for handlers
    String REF = "ref";
    String PHASELAST = "phaseLast";
    String PHASEFIRST = "phaseFirst";
    String PHASE = "phase";
    String OUT_FAILTFLOW = "Outfaultflow";    // faultflow start tag
    String OUTFLOWST = "outflow";         // outflowr start tag
    String ORDER = "order";           // to resolve the order tag
    String OPRATIONST = "operation";       // operation start tag

    String MODULE_PATH = "/modules/";
    String MESSAGERECEIVER = "messageReceiver";
    String MEP = "mep";

    // for jws file extension
    String JWS_EXTENSION = ".jws";
    String IN_FAILTFLOW = "INfaultflow";    // faultflow start tag
    String INFLOWST = "inflow";         // inflow start tag
    String HOTUPDATE = "hotupdate";
    String HOTDEPLOYMENT = "hotdeployment";
    String EXTRACTSERVICEARCHIVE = "extractServiceArchive";
    String DISPATCH_ORDER = "dispatchOrder";
    String DISPATCHER = "dispatcher";
    String DESCRIPTION = "description";
    String CONTEXTPATHNAME = "contextPath";
    String CLASSNAME = "class";
    String BEFORE = "before";
    String BEANMAPPINGST = "beanMapping";    // beanMapping start tag
    String AXIS2CONFIG = "axisconfig";
    String ATUSE = "use";

    // for parameters
    String ATTNAME = "name";
    String ATTLOCKED = "locked";

    // for operations
    String ATQNAME = "name";
    String AFTER = "after";
}
