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

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleOperation extends ConcreateCommonExecuter implements Operation{
    private Log log = LogFactory.getLog(getClass());   
    private QName name;
    private Handler provider;
    
    
    public SimpleOperation(QName name){
        this.name = name;
    }
    public void recive(MessageContext mc) throws AxisFault {
        super.recive(mc);
        Handler provider = getProvider();
        if(provider != null){
            log.info("invoking the Provider");
            provider.invoke(mc);            
        }
    }

    public void send(MessageContext mc) throws AxisFault {
        super.send(mc);
    }
    public QName getName() {
        return name;
    }

    public Handler getProvider() {
        return provider;
    }

    public void setProvider(Handler handler) {
        provider = handler;
    }

}
