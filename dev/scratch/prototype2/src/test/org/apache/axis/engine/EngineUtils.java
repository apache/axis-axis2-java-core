/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.engine;

import java.io.IOException;
import java.net.ServerSocket;

import javax.xml.namespace.QName;

import org.apache.axis.description.Flow;
import org.apache.axis.description.HandlerMetaData;
import org.apache.axis.impl.transport.http.SimpleHTTPReceiver;

/**
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public class EngineUtils {
    public static final int TESTING_PORT = 7777;
    public static final String FAILURE_MESSAGE = "Intentional Faliure";
    private static int index = 0; 
    
    public static void addHandlers(Flow flow,ExecutionChain exeChain,String phaseName) throws AxisFault{
        if(flow != null){
            int handlerCount = flow.getHandlerCount();
            for(int i = 0;i<handlerCount;i++){
                exeChain.addHandler(phaseName,flow.getHandler(i).getHandler());
            }
        }
    }
    
    public static SimpleHTTPReceiver startServer(EngineRegistry engineRegistry) throws IOException{
        AxisEngine engine = new AxisEngine(engineRegistry);
        ServerSocket serverSoc = new ServerSocket(TESTING_PORT);
        SimpleHTTPReceiver sas = new SimpleHTTPReceiver(engine);
        sas.setServerSocket(serverSoc);
        Thread thisThread = new Thread(sas);
        thisThread.setDaemon(true);
        thisThread.start();
        return sas;
    }
    
    public static void addHandler(Flow flow, Handler handler){
        HandlerMetaData hmd = new HandlerMetaData();
        hmd.setName(new QName("",String.valueOf(index)));
        index++;
        hmd.setHandler(handler);
        flow.addHandler(hmd);
    }

}
