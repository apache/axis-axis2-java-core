package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
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

    public void engage(CodeGenConfiguration configuration) throws CodeGenerationException {
        if (!configuration.isParametersWrapped()){
            //walk the schema and find the top level elements
            AxisService axisService = configuration.getAxisService();
            List schemaList = axisService.getSchema();
            for (int i = 0; i < schemaList.size(); i++) {
                walkSchema((XmlSchema)schemaList.get(i));
            }









        }
    }

    //walk the given schema
    public void walkSchema(XmlSchema schema){
        //get the schema and find the elements
        XmlSchemaObjectTable elementTable = schema.getElements();
        for (Iterator it = elementTable.getValues();
             it.hasNext();){
            //take the type of the schema element

        }



    }
}
