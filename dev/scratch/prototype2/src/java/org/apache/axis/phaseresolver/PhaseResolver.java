package org.apache.axis.phaseresolver;

import org.apache.axis.deployment.DeploymentEngine;
import org.apache.axis.deployment.metadata.ServerMetaData;
import org.apache.axis.description.*;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;

import javax.xml.namespace.QName;
import java.util.Vector;

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
 */
public class PhaseResolver {

    private EngineRegistry engineRegistry ;
    private AxisService axisService;
    private PhaseHolder phaseHolder ;


    private ServerMetaData server = DeploymentEngine.getServerMetaData();

    /**
     * default constructor , to obuild chains for AxisGlobal
     */
    public PhaseResolver(EngineRegistry engineRegistry) {
        this.engineRegistry = engineRegistry;
    }

    public PhaseResolver(EngineRegistry engineRegistry, AxisService axisService ) {
        this.engineRegistry = engineRegistry;
        this.axisService = axisService;
    }

    public void buildchains() throws PhaseException, AxisFault {
        for(int i =1 ; i < 4 ; i++) {
            buildExcutionChains(i);

        }

    }

    /**
     * this opeartion is used to build all the three cahins ,
     * so type varible is used to difrenciate them
     *  type = 1 inflow
     *  type = 2 out flow
     *  type = 3 fault flow
     * @param type
     * @throws AxisFault
     */
    private  void buildExcutionChains(int type) throws AxisFault, PhaseException {
        int flowtype =  type;
        Vector allHandlers = new Vector();
        int count = server.getModuleCount();
        QName moduleName;
        AxisModule module;
        Flow flow = null;
        /*
        //adding server specific handlers  . global
        for(int intA=0 ; intA < count; intA ++){
        moduleName = server.getModule(intA);
        module = engineRegistry.getModule(moduleName);
        switch (flowtype){
        case 1 : {
        flow = module.getInFlow();
        break;
        }
        case  2 : {
        flow = module.getOutFlow();
        break;
        }
        case 3 : {
        flow = module.getFaultFlow();
        break;
        }
        }
        for(int j= 0 ; j < flow.getHandlerCount() ; j++ ){
        HandlerMetaData metadata = flow.getHandler(j);
        //todo change this in properway
        if (metadata.getRules().getPhaseName().equals("")){
        metadata.getRules().setPhaseName("global");
        }
        allHandlers.add(metadata);
        }
        }
        */
        // service module handlers
        Vector modules = (Vector)axisService.getModules();
        for (int i = 0; i < modules.size(); i++) {
            QName moduleref = (QName) modules.elementAt(i);
            module = engineRegistry.getModule(moduleref);
            switch (flowtype){
                case 1 : {
                    flow = module.getInFlow();
                    break;
                }
                case  2 : {
                    flow = module.getOutFlow();
                    break;
                }
                case 3 : {
                    flow = module.getFaultFlow();
                    break;
                }
            }
            for(int j= 0 ; j < flow.getHandlerCount() ; j++ ){
                HandlerMetaData metadata = flow.getHandler(j);
                //todo change this in properway
                if (metadata.getRules().getPhaseName().equals("") ){
                    metadata.getRules().setPhaseName("service");
                }
                allHandlers.add(metadata);
            }

        }

        switch (flowtype){
            case 1 : {
                flow = axisService.getInFlow();
                break;
            }
            case  2 : {
                flow = axisService.getOutFlow();
                break;
            }
            case 3 : {
                flow = axisService.getFaultFlow();
                break;
            }
        }
        for(int j= 0 ; j < flow.getHandlerCount() ; j++ ){
            HandlerMetaData metadata = flow.getHandler(j);
            //todo change this in properway
            if (metadata.getRules().getPhaseName().equals("")){
                metadata.getRules().setPhaseName("service");
            }
            allHandlers.add(metadata);
        }

        phaseHolder = new PhaseHolder(server,axisService);

        for (int i = 0; i < allHandlers.size(); i++) {
            HandlerMetaData handlerMetaData = (HandlerMetaData) allHandlers.elementAt(i);
            phaseHolder.addHandler(handlerMetaData);
        }
        phaseHolder.getOrderdHandlers(type);

    }

    public void buildGlobalChains(AxisGlobal global) throws AxisFault, PhaseException {
        Vector modules = (Vector)global.getModules();
        int count = modules.size();
        QName moduleName;
        AxisModule module;
        Flow flow = null;
        for(int type = 1 ; type < 4 ; type ++){
            phaseHolder = new PhaseHolder(server,null);
            for(int intA=0 ; intA < count; intA ++){
                moduleName = (QName)modules.get(intA);
                module = engineRegistry.getModule(moduleName);
                switch (type){
                    case 1 : {
                        flow = module.getInFlow();
                        break;
                    }
                    case  2 : {
                        flow = module.getOutFlow();
                        break;
                    }
                    case 3 : {
                        flow = module.getFaultFlow();
                        break;
                    }
                }
                for(int j= 0 ; j < flow.getHandlerCount() ; j++ ){
                    HandlerMetaData metadata = flow.getHandler(j);
                    //todo change this in properway
                    if (metadata.getRules().getPhaseName().equals("")){
                        metadata.getRules().setPhaseName("global");
                    }
                    phaseHolder.addHandler(metadata);

                }

            }
            phaseHolder.buildGoblalChain(global, type);
        }
    }

}
