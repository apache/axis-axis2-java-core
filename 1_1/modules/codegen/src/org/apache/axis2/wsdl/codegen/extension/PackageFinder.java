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

package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;

public class PackageFinder extends AbstractCodeGenerationExtension {


    public void engage(CodeGenConfiguration configuration) {
        String packageName = configuration.getPackageName();
        if (packageName == null || URLProcessor.DEFAULT_PACKAGE.equals(packageName)) {
            //use the target namespace from the axis service to form a package
            //name
            packageName = URLProcessor.makePackageName(
                    configuration.getAxisService().getTargetNamespace()
            );
        }

        if (null == packageName || "".equals(packageName))
            packageName = URLProcessor.DEFAULT_PACKAGE;

        configuration.setPackageName(packageName.toLowerCase());

    }


}