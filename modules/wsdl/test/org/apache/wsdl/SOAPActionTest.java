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

package org.apache.wsdl;

import java.util.Iterator;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.SOAPOperation;

/**
 * @author chathura@opensource.lk
 */
public class SOAPActionTest extends AbstractTestCase {

    private WSDLDescription womDescription = null;

    private Definition wsdl4jDefinition = null;

    public SOAPActionTest(String args) {
        super(args);
    }

    protected void setUp() throws Exception {
        super.setUp();
        WSDLVersionWrapper wsdlVersionWrapper = null;
        if (null == this.womDescription) {
            String path = getTestResourceFile("InteropTestDocLit2.wsdl").getAbsolutePath();
            wsdlVersionWrapper =
                    WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11)
                    .build(path);
            this.womDescription = wsdlVersionWrapper.getDescription();

        }
        if (null == wsdl4jDefinition) {
            this.wsdl4jDefinition = wsdlVersionWrapper.getDefinition();
        }
    }

    public void testSOAPActionPopulation() {
        WSDLBindingOperation bindingOperation = womDescription
                .getFirstBinding().getBindingOperation(new QName(
                        "http://soapinterop.org/WSDLInteropTestDocLit",
                        "echoVoid"));
        Iterator iterator = bindingOperation.getExtensibilityElements()
                .iterator();
        while (iterator.hasNext()) {
            WSDLExtensibilityElement element = (WSDLExtensibilityElement) iterator
                    .next();
            SOAPOperation soapOperation = null;
            if (ExtensionConstants.SOAP_OPERATION.equals(element.getType())) {
                soapOperation = (SOAPOperation) element;
            }
            if (soapOperation == null) {
                fail();
            } else {
                assertEquals(soapOperation.getSoapAction(),
                        "http://soapinterop.org/");
            }
        }
    }

}