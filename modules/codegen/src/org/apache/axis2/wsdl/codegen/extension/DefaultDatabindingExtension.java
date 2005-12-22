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

import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.TypeMapper;

public class DefaultDatabindingExtension extends AbstractDBProcessingExtension {


    public void engage() throws CodeGenerationException {
        TypeMapper mappper = configuration.getTypeMapper();
        if (testFallthrough(configuration.getDatabindingType())) {
            //if it's fall through for the default databinding extension and a mapper has
            //not yet being set, then there's a problem.
            //Hence check the mapper status here

            if (mappper == null) {
                //this shouldn't happen
                throw new CodeGenerationException("No proper databinding has taken place");
            }
            return;
        }

        configuration.setTypeMapper(new DefaultTypeMapper());
    }
}
