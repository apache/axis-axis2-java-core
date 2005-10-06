package org.apache.axis2.databinding.gen.impl;

import org.apache.axis2.databinding.gen.GeneratorFactory;
import org.apache.axis2.databinding.gen.Generator;
import org.apache.axis2.databinding.gen.Parser;
import org.apache.axis2.databinding.symbolTable.TypeEntry;
import org.apache.axis2.databinding.symbolTable.SymbolTable;
import org.apache.axis2.databinding.symbolTable.BaseTypeMapping;
import org.apache.axis2.databinding.toJava.JavaTypeWriter;
import org.apache.axis2.databinding.toJava.Emitter;
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

public class GeneratorFactoryImpl implements GeneratorFactory {

     private Parser parser = null;

    /**
     * Method getGenerator
     *
     * @param type
     * @param symbolTable
     * @return
     */
    public Generator getGenerator(TypeEntry type, SymbolTable symbolTable) {
        return new JavaTypeWriter(new Emitter(), type, symbolTable);
    }    // getGenerator

    public void setBaseTypeMapping(BaseTypeMapping btm) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public BaseTypeMapping getBaseTypeMapping() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
