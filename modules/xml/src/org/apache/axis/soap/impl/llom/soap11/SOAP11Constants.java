package org.apache.axis.soap.impl.llom.soap11;

import org.apache.axis.soap.impl.llom.SOAPConstants;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 */
public interface SOAP11Constants extends SOAPConstants{
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */
   public static final String SOAP_ENVELOPE_NAMESPACE_URI = "http://schemas.xmlsoap.org/soap/envelope/";
   
   /**
    * Field ATTR_ACTOR
    */
   public static final String ATTR_ACTOR = "actor";
   
   /**
    * Field SOAP_FAULT_CODE_LOCAL_NAME
    */
   public static final String SOAP_FAULT_CODE_LOCAL_NAME = "faultcode";
   /**
    * Field SOAP_FAULT_STRING_LOCAL_NAME
    */
   public static final String SOAP_FAULT_STRING_LOCAL_NAME = "faultstring";
   /**
    * Field SOAP_FAULT_ACTOR_LOCAL_NAME
    */
   public static final String SOAP_FAULT_ACTOR_LOCAL_NAME = "faultactor";

   public static final String SOAP_FAULT_DETAIL_LOCAL_NAME = "detail";
}
