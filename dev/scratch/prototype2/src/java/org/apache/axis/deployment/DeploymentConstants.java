package org.apache.axis.deployment;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Oct 18, 2004
 *         12:54:57 PM
 *
 */

/**
 * DeployCons interface is to keep constent value required for Deployemnt
 */
public interface DeploymentConstants {
    int SERVICE = 0; // if it is a servise
    int MODULE = 1; // if it is a module

    String SERVICEXML = "META-INF/service.xml";
    String MODULEXML = "META-INF/module.xml";
    String PARAMETERST = "parameter";// paramater start tag
    String HANDERST = "handler";
    String MODULEST ="module";
    String PHASEST = "phase";
    String PHASE_ORDER = "phaseOrder";
    String TYPEMAPPINGST = "typeMapping";// typeMapping start tag
    String BEANMAPPINGST = "beanMapping";// beanMapping start tag
    String OPRATIONST = "operation";// operation start tag
    String INFLOWST = "inflow";// inflow start tag
    String OUTFLOWST = "outflow";// outflowr start tag
    String FAILTFLOWST = "faultflow";// faultflow start tag

    String FOLDE_NAME = "D:/Axis 2.0/projects/Deployement/test-data";
    String MODULE_PATH = "/modules/";
    String SERVICE_PATH = "/services/";

    // for jws file extension
    String JWS_EXTENSION = ".jws";

    String SERVICETAG = "service";


}
