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
package org.apache.axis.registry;

/**
 * <p> This make sure all the CommonExecuters of given type in the pool share a common state. 
 * Changing one will affet all. e.g. All the Transport instances share a comman state.</p>  
 *  
 * @author hemapani@opensource.lk
 * 
 */
public class CommonExecuterStateFactory {
    private static CommonExecuterState serviceState;
    private static CommonExecuterState operationState;
    private static CommonExecuterState transportState;
    private static CommonExecuterState globalState;            
    
    public static CommonExecuterState getServiceState(){
        if(serviceState == null)
            return new CommonExecuterState();
        return serviceState;  
        
    }
    public static CommonExecuterState getTrasportState(){
        if(transportState == null)
            return new CommonExecuterState();
        return transportState;  
        
    }
    public static CommonExecuterState getGlobalState(){
        if(globalState == null)
            return new CommonExecuterState();
        return globalState;  
        
    }
    public static CommonExecuterState getOperationState(){
        if(operationState == null)
            return new CommonExecuterState();
        return operationState;  
        
    }
    
}
