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
package org.apache.axis2.databinding.toJava;

//import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.apache.axis2.databinding.symbolTable.TypeEntry;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * This is Wsdl2java's Holder Writer.  It writes the <typeName>Holder.java file.
 */
public class JavaHolderWriter extends JavaClassWriter {

    /** Field type */
    private TypeEntry type;

    /**
     * Constructor.
     * 
     * @param emitter 
     * @param type    
     */
    protected JavaHolderWriter(Emitter emitter, TypeEntry type) {

        super(emitter, Utils.holder(type, emitter), "holder");

        this.type = type;
    }    // ctor

    /**
     * Return "public final ".
     * 
     * @return 
     */
    protected String getClassModifiers() {
        return super.getClassModifiers() + "final ";
    }    // getClassModifiers

    /**
     * Return "implements javax.xml.rpc.holders.Holder ".
     * 
     * @return 
     */
    protected String getImplementsText() {
        return "implements javax.xml.rpc.holders.Holder ";
    }    // getImplementsText

    /**
     * Generate the holder for the given complex type.
     * 
     * @param pw 
     * @throws IOException 
     */
    protected void writeFileBody(PrintWriter pw) throws IOException {

        String holderType = type.getName();

        pw.println("    public " + holderType + " value;");
        pw.println();
        pw.println("    public " + className + "() {");
        pw.println("    }");
        pw.println();
        pw.println("    public " + className + "(" + holderType + " value) {");
        pw.println("        this.value = value;");
        pw.println("    }");
        pw.println();
    }    // writeOperation

    /** Generate a java source file for the holder class.
     * If the emitter works in deploy mode and the class already exists, the source wull not be generated.
     */
    public void generate() throws IOException {
        String fqcn = getPackage() + "." + getClassName();
        if (emitter.isDeploy()) {
            if (!emitter.doesExist(fqcn)) {
                super.generate();
            }
        } else {
            super.generate();
        }
    }
}    // class JavaHolderWriter
