package org.apache.axis2.handlers.addressing;

import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 *
 */

public abstract class AddressingHandler extends AbstractHandler implements AddressingConstants {

    // this parameter has to be set by the module deployer.
    protected boolean isAddressingOptional = true;

    protected String addressingNamespace = Final.WSA_NAMESPACE;  // defaulting to final version
    protected String addressingVersion = null;

    protected static final Log log = LogFactory.getLog(AddressingHandler.class);

}
