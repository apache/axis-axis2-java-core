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

package org.apache.axis2.security;

import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;


public class AddressingMTOMSecurityTest extends InteropTestBase {

    protected OutflowConfiguration getOutflowConfiguration() {

        OutflowConfiguration ofc = new OutflowConfiguration();

        ofc.setActionItems("Timestamp Signature Encrypt");
        ofc.setUser("alice");
        ofc.setEncryptionUser("bob");
        ofc.setSignaturePropFile("interop.properties");
        ofc.setPasswordCallbackClass("org.apache.axis2.security.PWCallback");
        ofc.setSignatureKeyIdentifier(WSSHandlerConstants.SKI_KEY_IDENTIFIER);
        ofc.setEncryptionKeyIdentifier(WSSHandlerConstants.SKI_KEY_IDENTIFIER);
        ofc.setSignatureParts("{Element}{" + ADDR_NS + "}To;" +
                                "{Element}{" + ADDR_NS + "}ReplyTo;" +
                                "{Element}{" + ADDR_NS + "}MessageID;" +
                                "{Element}{" + WSU_NS + "}Timestamp");
        ofc.setOptimizeParts(
                "//xenc:EncryptedData/xenc:CipherData/xenc:CipherValue");

        return ofc;
    }

    protected InflowConfiguration getInflowConfiguration() {
        InflowConfiguration ifc = new InflowConfiguration();

        ifc.setActionItems("Timestamp Signature Encrypt");
        ifc.setPasswordCallbackClass("org.apache.axis2.security.PWCallback");
        ifc.setSignaturePropFile("interop.properties");

        return ifc;
    }

    protected String getClientRepo() {
        return COMPLETE_CLIENT_REPOSITORY;
    }

    protected String getServiceRepo() {
        return COMPLETE_SERVICE_REPOSITORY;
    }

    protected boolean isUseSOAP12InStaticConfigTest() {
        return true;
    }

}
