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
*/


package org.apache.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;

public class RESTCall extends Call {
    public RESTCall() throws AxisFault {
        super();
    }

    public RESTCall(ServiceContext service) {
        super(service);
    }

    public OMElement invokeBlocking() throws AxisFault {
        return super.invokeBlocking("nothing",
                OMAbstractFactory.getOMFactory().createOMElement("nothing",
                        "nothing", "nothing"));
    }

    public void invokeNonBlocking(Callback callback) throws AxisFault {
        super.invokeNonBlocking("nothing",
                OMAbstractFactory.getOMFactory().createOMElement("nothing",
                        "nothing", "nothing"), callback);
    }
}
