/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis;

import org.apache.axis.context.MessageContext;
import org.apache.axis.registry.EngineElement;

/**
 * Common Excecuter is self contianed. It should handle all the funcations
 * it self. It handles following
 * <ol>
 *  <li>Ordering of the Handlers inside itself.</li>
 *  <li>If error occured inside the send(..),recive(..) the comman executer should 
 * rollback the the handlers and throw the exeception.</li> 
 * </ol>
 */
public interface CommonExecutor extends EngineElement{
    public static final String TRANSPORT_WORKER = "trnsport";
    public static final String GLOBEL_WORKER = "globel";
    public static final String SERVICE_WORKER = "service";
    
    public void send(MessageContext mc)throws AxisFault;
    public void recive(MessageContext mc)throws AxisFault;
    public void rollback(MessageContext mc)throws AxisFault;
    public void processFaultFlow(MessageContext mc)throws AxisFault;
    
}
