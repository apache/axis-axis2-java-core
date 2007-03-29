/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.sample.parallelasync.common;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import javax.xml.ws.Service;

public class KillerThread extends Thread{

    private Service svc = null;
    
    private boolean isKilled = false;
    private boolean interrupt = false;
    
    private int waitUntilKillingSec = 30;
    
    public KillerThread(Service service, int client_max_sleep_sec) {
        this.waitUntilKillingSec = client_max_sleep_sec;
        this.svc = service;
    }

    public void run(){
        Executor e = svc.getExecutor();
        
        System.out.println("KillerThread: " + e.getClass().getName());
        ExecutorService es = (ExecutorService) e;
        

        int i = waitUntilKillingSec;
        while (i > 0 && !interrupt){
                    
            try {
                System.out.println("KillerThread: going to sleep");
                Thread.sleep(1000);
                i --;
            } catch (InterruptedException e1) {
                System.out.println("KillerThread: interrupted");
            }
        }
        
        // force executor to stop
        if (!interrupt){
            isKilled = true;
            es.shutdownNow();
        }
    }

    public boolean isKilled() {
        return isKilled;
    }
    
    public void abort(){
        this.interrupt = true;
        this.interrupt();
    }
}
