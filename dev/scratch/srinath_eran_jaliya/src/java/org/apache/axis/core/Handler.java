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

package org.apache.axis.core;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.apache.axis.core.context.MessageContext;
import org.apache.axis.core.registry.NamedEngineElement;

public interface Handler extends Serializable,NamedEngineElement {
    /**
     * Invoke is called to do the actual work of the Handler object.
     * If there is a fault during the processing of this method it is
     * invoke's job to catch the exception and undo any partial work
     * that has been completed.  Once we leave 'invoke' if a fault
     * is thrown, this classes 'onFault' method will be called.
     * Invoke should rethrow any exceptions it catches, wrapped in
     * an AxisFault.
     *
     * @param msgContext    the <code>MessageContext</code> to process with this
     *              <code>Handler</code>.
     * @throws AxisFault if the handler encounters an error
     */
    public void invoke(MessageContext msgContext) throws AxisFault ;

    /**
     * Called when a subsequent handler throws a fault.
     *
     * @param msgContext    the <code>MessageContext</code> to process the fault
     *              to
     */
    public void revoke(MessageContext msgContext);

    public void setName(QName name);
    public QName getName();
};
