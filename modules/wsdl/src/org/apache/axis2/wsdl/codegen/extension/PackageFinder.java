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

package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CommandLineOption;
import org.apache.axis2.wsdl.codegen.CommandLineOptionConstants;
import org.apache.axis2.wsdl.util.URLProcessor;
import org.apache.wsdl.WSDLBinding;

import java.util.Map;

/**
 * @author chathura@opensource.lk
 */
public class PackageFinder extends AbstractCodeGenerationExtension {

    public void init(CodeGenConfiguration configuration) {
        this.configuration = configuration;

    }

    public void engage() {
        String packageName = this.configuration.getPackageName();
        if (packageName == null) {
            WSDLBinding binding = configuration.getWom().getBinding(
                    AxisBindingBuilder.AXIS_BINDING_QNAME);
            String temp = binding.getBoundInterface().getName()
                    .getNamespaceURI();
            packageName = URLProcessor.getNameSpaceFromURL(temp);
        }

        if (null == packageName || "".equals(packageName))
            packageName = URLProcessor.DEFAULT_PACKAGE;

        this.configuration.setPackageName(packageName.toLowerCase());

    }


}