package org.apache.axis2.wsdl.java2wsdl;

import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.writer.WOMWriter;
import org.apache.axis2.wsdl.writer.WOMWriterFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.wsdl.WSDLDescription;

import java.io.OutputStream;
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
* @author : Deepal Jayasinghe (deepal@apache.org)
*
*/

public class Java2WSDL {

    private OutputStream out;
    private String className;
    private ClassLoader classLoader;

    public Java2WSDL(OutputStream out, String className, ClassLoader classLoader) {
        this.out = out;
        this.className = className;
        this.classLoader = classLoader;
    }

    public void generateWSDL() throws Exception {
        SchemaGenerator sg = new SchemaGenerator(classLoader, className);
        XmlSchema scheam = sg.generateSchema();
        WSDLDescription wommodel = new Java2WOM(
                sg.getTypeTable(), sg.getMethods(), scheam, className).generateWOM();
        WOMWriter womWriter = WOMWriterFactory.createWriter(WSDLConstants.WSDL_1_1);
        womWriter.setdefaultWSDLPrefix("wsdl");
        womWriter.writeWOM(wommodel, out);

    }
}
