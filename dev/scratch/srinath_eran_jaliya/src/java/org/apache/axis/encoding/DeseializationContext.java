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
package org.apache.axis.encoding;

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaders;

/**
 * <p>All the Deserialization Happen through the DeSerializationContext interface. 
 * The Axis Transport shuould create and add the DeserializationContexts for 
 * the Message Context. The Output Streams are hidden behind this. This is the only
 * Way the engine can read something out.</p>
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public interface DeseializationContext {
    public SOAPEnvelope parseEnvelope()throws AxisFault;
    
    public SOAPHeaders parseHeaders() throws AxisFault;
    public QName enterTheBody(int style) throws AxisFault;
}
