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

package org.apache.axis.engine;

import org.apache.axis.context.MessageContext;

import javax.xml.namespace.QName;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class ServiceLocator {
    public static Service locateService(String uri,String soapAction,MessageContext msgctx) throws AxisFault{
        QName serviceName = null;
        if(uri != null){
            int index = uri.indexOf('?');
            if(index > -1){
                //TODO get the opeartion name from URI as well 
                serviceName = new QName(uri);
            }else{
                serviceName = new QName(uri);
            }
        }else{
            if(soapAction != null){
                serviceName = new QName(uri);
            }
        }
        if(serviceName != null){
            Service service = msgctx.getGlobalContext().getRegistry().getService(serviceName);
            if(service != null){
                return service;
            }else{
                throw new AxisFault("Service Not found");
            }
        }else{
            throw new AxisFault("Both the URI and SOAP_ACTION Is Null");
        }
    }
}
