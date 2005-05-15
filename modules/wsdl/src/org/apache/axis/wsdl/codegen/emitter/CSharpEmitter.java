package org.apache.axis.wsdl.codegen.emitter;

import org.apache.axis.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis.wsdl.databinding.CsharpTypeMapper;
import org.apache.axis.wsdl.databinding.TypeMapper;
import org.apache.axis.wsdl.databinding.DefaultTypeMapper;
import org.apache.crimson.tree.XmlDocument;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Iterator;

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
 * 
 */
public class CSharpEmitter extends MultiLanguageClientEmitter{

    public CSharpEmitter(CodeGenConfiguration configuration) {
        this.configuration = configuration;
//        this.mapper = new CsharpTypeMapper();
        this.mapper = new DefaultTypeMapper();

    }

     public CSharpEmitter(CodeGenConfiguration configuration,TypeMapper mapper) {
        this.configuration = configuration;
        this.mapper =mapper;

    }

   
}
