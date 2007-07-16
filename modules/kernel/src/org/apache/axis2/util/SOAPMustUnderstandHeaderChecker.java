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
package org.apache.axis2.util;

import org.apache.axis2.context.MessageContext;

import java.util.HashMap;

/**
 * Interface to allow participation in the engine's validation of SOAP headers marked as 
 * mustUnderstand.  This allows components such as MessageReceivers to indicate that they WILL
 * process specific headers later, even though those headers have not yet been marked as
 * processed.  
 * 
 * Implementations of this interface are specified in the axis2.xml configuration
 * file using the following elements:
 *     <soapMustUnderstandCheckers>
 *         <soapMustUnderstandChecker class="class" />
 *     </soapMustUnderstandCheckers>
 *     
 * Implementations are called during the engine's mustUnderstand validation with a collection of 
 * headers that are not yet understood.  Note that only headers for the roles/actors in which this
 * engine is acting will be in the collection.  The implementation should remove any headers 
 * which it understands.  Note that headers which have already been processed will be include in
 * the collection, but do not need to be removed (although they can be; it doesn't matter either
 * way).  The checker returns the collection with the headers it understands removed.  That
 * collection is passed to the next checker.
 * 
 * After all the checkers have been called by the engine, it will examine the collection for any
 * headers that are marked mustUndertand which are not marked as processed.  If any exist, it will
 * throw a SOAP mustUnderstand fault. 
 */
public interface SOAPMustUnderstandHeaderChecker {
    /**
     * Remove any headers which this checkers understands from the collection. 
     * 
     * @param msgContext The Message Context for the current message
     * @param headers A collection of headers for this actor/role
     * @return The collection with any headers understood by this checker removed.
     */
    HashMap removeUnderstoodHeaders(MessageContext msgContext, HashMap headers);
}
