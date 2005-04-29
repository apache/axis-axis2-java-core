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
 *
 *  Runtime state of the engine
 */
package org.apache.axis.context;

import org.apache.axis.description.AxisOperation;
import org.apache.axis.engine.AxisFault;
import org.apache.wsdl.WSDLConstants;

public class MEPContextFactory implements WSDLConstants{
    
    public static MEPContext createMEP(String mepURI,boolean serverSide,AxisOperation axisOp) throws AxisFault{
        if(MEP_URI_IN_ONLY.equals(mepURI) || MEP_URI_IN_OUT.equals(mepURI)){
            return new BasicMEPContext(axisOp);
        
        }else{
        	throw new AxisFault("Cannot handle the MEP "
        			+ mepURI+" for the current invocation of Operation ");
        }
    }
    
}
