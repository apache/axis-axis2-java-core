package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;

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
 *  The function of this class is to fill the default information if it's not already filled
 */
public class DefaultDatabindingExtension extends AbstractCodeGenerationExtension {
    private CodeGenConfiguration configuration;
    public void init(CodeGenConfiguration configuration) {
        this.configuration = configuration;
    }

    public void engage() throws CodeGenerationException {
        TypeMapper mappper = configuration.getTypeMapper();
        if (mappper==null){
            configuration.setTypeMapper(new DefaultTypeMapper());
        }
    }
}
