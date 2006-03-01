package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.builder.SchemaUnwrapper;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
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

/**
 * This extension invlokes the schema unwrapper depending on the users setting.
 * it is desirable to put this extension before other extensions since extnsions
 * such as the databinding extension may well depend on the schema being unwrapped
 * previously
 */
public class SchemaUnwrapperExtension extends AbstractCodeGenerationExtension {

    public void engage() throws CodeGenerationException {
          if (!configuration.isParametersWrapped()){
              //unwrap the schema since we are told to do so
             SchemaUnwrapper.unwrap(configuration.getWom());
          }
    }
}
