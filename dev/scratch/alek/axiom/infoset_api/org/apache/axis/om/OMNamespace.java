/*
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.axis.om;


/**
 * Represents
 * <a href="http://www.w3.org/TR/xml-infoset/#infoitem.namespace">Namespace Information Item</a>.
 */
public interface OMNamespace {
    /**
     * Prefix can be null.
     * In this case it will be looked up from XML tree
     * and used if available
     * otherwise it will be automatically created only for serializaiton.
     * TODO: If prefix is empty string it will be used to indicate default namespace.
     */
    public String getPrefix();

    /**
     * Namespace name.
     * Never null.
     * Only allowed to be empty string if prefix is also empty string
     * (used to undeclare default namespace)
     */
    public String getNamespaceName();
}
