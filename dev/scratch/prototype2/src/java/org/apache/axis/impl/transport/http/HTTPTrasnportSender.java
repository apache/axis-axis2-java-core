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

package org.apache.axis.impl.transport.http;

import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.AbstractTrasnportSender;
import org.apache.axis.addressing.EndpointReferenceType;

import java.io.OutputStream;
import java.net.URL;

public class HTTPTrasnportSender extends AbstractTrasnportSender {
    protected OutputStream out;

    public HTTPTrasnportSender(OutputStream out) {
        this.out = out;
    }

    protected OutputStream obtainOutPutStream(MessageContext msgContext) throws AxisFault {
        OutputStream out = (OutputStream) msgContext.getProperty(MessageContext.TRANSPORT_DATA);
        if (out == null) {
            throw new AxisFault("can not find the suffient information to find endpoint");
        } else {
            return out;
        }

    }

    protected OutputStream obtainOutPutStream(MessageContext msgContext, EndpointReferenceType epr) {
       throw new UnsupportedOperationException("Addressing not suppotrted yet");
    }

    protected void finalizeSending() {
    }

    protected void startSending() {
//      if(!msgContext.isServerSide()){
//          URL url = (URL)msgContext.getProperty(MessageContext.REQUEST_URL);
//          if(url != null){
//              StringBuffer buf = new StringBuffer();
//              buf.append("POST ").append(url.getFile()).append("HTTP/1.1\n");
//              buf.append("Host: ").append(url.getHost());
//              buf.append("Content-Type: application/soap+xml; charset=\"utf-8\"\n");
//              out.write(buf.toString().getBytes());
//          }else{
//              throw new AxisFault(MessageContext.REQUEST_URL + "where to send ?");
//          }
//      }
    }

}
