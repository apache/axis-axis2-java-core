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

package org.apache.axis2.jaxws.sample.parallelasync.server;

import java.util.concurrent.Future;

import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;

import org.apache.axis2.jaxws.sample.parallelasync.common.Constants;
import org.test.parallelasync.AnotherResponse;
import org.test.parallelasync.CustomAsyncResponse;
import org.test.parallelasync.InvokeAsyncResponse;
import org.test.parallelasync.PingResponse;
import org.test.parallelasync.SleepResponse;

/**
 * Async endpoint used for Async client side tests. Clients will invokeAsync
 * sleep method to force the server to block until wakeUp is called. The client
 * can call isAsleep to verify that sleep has been called by the async thread.
 */
/*
@WebService(
        serviceName="AsyncService",
        portName="AsyncPort",
        targetNamespace = "http://org/test/parallelasync",
        endpointInterface = "org.test.parallelasync.AsyncPort",
        wsdlLocation="WEB-INF/wsdl/async_doclitwr.wsdl")
        */
@WebService(endpointInterface="org.apache.axis2.jaxws.sample.parallelasync.server.AsyncPort")
public class DocLitWrappedPortImpl implements AsyncPort {

    private static final boolean DEBUG = true;

    private static String msg = "";

    private static Thread sleeper = null;

    private static boolean doCancell = false;

    public void sleep(Holder<String> message) {

        boolean cancelRequested = false;

        msg = message.value;

        synchronized (msg) {

            Thread myThread = Thread.currentThread();
            sleeper = myThread;

            try {

                doCancell = false;

                if (DEBUG)
                    System.out.println("Starting to sleep on "
                            + myThread.getId() + " " + msg);

                // wait until either a timeout or client releases us
                // or if another request begins (lockId changes)
                long sec = Constants.SERVER_SLEEP_SEC;

                while (sec > 0 && !doCancell && sleeper == myThread) {
                    if (DEBUG)
                        System.out.println("Sleeping on " + myThread.getId()
                                + " timeLeft=" + sec);
                    sec--;

                    msg.wait(500);
                }
            } catch (InterruptedException e) {
                System.out.println("Sleep interrupted on " + myThread.getId());
            } finally {

                if (DEBUG)
                    System.out.println("Woke up " + myThread.getId());

                // if we timed out while waiting then
                // release the wait
                if (sleeper == myThread) {
                    cancelRequested = doCancell;
                    doCancell = false;
                    sleeper = null;
                }

                // only notify if cancel was requested
                if (cancelRequested) {
                    if (DEBUG)
                        System.out.println("Notify " + myThread.getId()
                                + " isDone");

                    // wake up the release thread
                    msg.notify();
                }
            }
        }// synch
    }

    public String isAsleep() {

        // return the message we're sleeping on or if we aren't return a null
        return (sleeper != null) ? msg : null;
    }

    public String wakeUp() {

        String wakeUp = null;

        if (sleeper == null) {
            if (DEBUG)
                System.out.println("No one to wake up");
        } else {
            if (DEBUG)
                System.out.println("Interrupting " + sleeper.getId());

            // interrupt the sleeper
            sleeper.interrupt();

            if (DEBUG)
                System.out.println("release before synched");

            // block until sleep completes
            synchronized (msg) {

                if (DEBUG)
                    System.out.println("release enter synched");

                wakeUp = msg;
                msg = null;
            }
        }

        return wakeUp;
    }

    /**
     * client side tests for remapping operation names, on the server side all
     * we need to do is roundtrip the message
     */

    public String invokeAsync(String request) {
        return request;
    }

    public String customAsync(String request) {
        return request;
    }

    public String another(String request) {
        return request;
    }

    public String ping(String message) {
        return message;
    }

    // NOT USED:
    
    public String anotherAsync(String request) {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<?> anotherAsyncAsync(String request, AsyncHandler<AnotherResponse> asyncHandler) {
        // TODO Auto-generated method stub
        return null;
    }

    public Response<AnotherResponse> anotherAsyncAsync(String request) {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<?> invokeAsyncAsync(String request, AsyncHandler<InvokeAsyncResponse> asyncHandler) {
        // TODO Auto-generated method stub
        return null;
    }

    public Response<InvokeAsyncResponse> invokeAsyncAsync(String request) {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<?> pingAsync(String message, AsyncHandler<PingResponse> asyncHandler) {
        // TODO Auto-generated method stub
        return null;
    }

    public Response<PingResponse> pingAsync(String message) {
        // TODO Auto-generated method stub
        return null;
    }

    public String remapped(String request) {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<?> remappedAsync(String request, AsyncHandler<CustomAsyncResponse> asyncHandler) {
        // TODO Auto-generated method stub
        return null;
    }

    public Response<CustomAsyncResponse> remappedAsync(String request) {
        // TODO Auto-generated method stub
        return null;
    }

    public Future<?> sleepAsync(String message, AsyncHandler<SleepResponse> asyncHandler) {
        // TODO Auto-generated method stub
        return null;
    }

    public Response<SleepResponse> sleepAsync(String message) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
