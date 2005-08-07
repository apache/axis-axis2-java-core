package org.apache.axis2.om.impl;

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
 * author : Eran Chinthaka (chinthaka@apache.org)
 */

public interface MTOMConstants {
    public static final String XOP_INCLUDE = "Include";
    public static final String XOP_NAMESPACE_URI = "http://www.w3.org/2004/08/xop/include";

    /**
     * if the Message is MTOM optimised then <code>MTOM_TYPE</code>
     */
    String MTOM_TYPE = "application/xop+xml";
    /**
     * If the message is Soap with Attachments <code>SwA_TYPE</code>
     */
    String SWA_TYPE = "text/xml";
    /**
     * <code>rootPart</code> is used as the key for the root BodyPart in the
     * Parts HashMap
     */
    String ROOT_PART = "SoapPart";
    String ATTACHMENTS = "Attachments";
}
