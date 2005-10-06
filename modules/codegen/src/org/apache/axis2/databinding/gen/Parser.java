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
package org.apache.axis2.databinding.gen;


import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.apache.axis2.databinding.symbolTable.*;
import org.apache.axis2.databinding.gen.impl.GeneratorFactoryImpl;
import org.apache.wsdl.WSDLDescription;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * This is a class with no documentation.
 */
public class Parser {

    /** Field debug */
    protected boolean debug = true;

    /** Field quiet */
    protected boolean quiet = false;

    /** Field imports */
    protected boolean imports = true;

    /** Field verbose */
    protected boolean verbose = false;

    /** Field nowrap */
    protected boolean nowrap = false;

    // Username and password for Authentication

    /** Field username */
    protected String username = null;

    /** Field password */
    protected String password = null;

    /** If this is false, we'll prefer "String[]" to "ArrayOfString" for literal wrapped arrays */
    protected boolean wrapArrays = false;

    // Timeout, in milliseconds, to let the Emitter do its work

    /** Field timeoutms */
    private long timeoutms = 45000;    // 45 sec default

    /** Field genFactory */
    private GeneratorFactory genFactory = new GeneratorFactoryImpl();

    /** Field symbolTable */
    private SymbolTable symbolTable = null;

    //constructor
    public Parser() {
        symbolTable = new SymbolTable(new BaseTypeMapping() {

                public String getBaseName(QName qName) {
                    return "java.lang.Object";
                }
            },true);
    }

    /**
     * Method isDebug
     * 
     * @return
     */
    public boolean isDebug() {
        return debug;
    }    // isDebug

    /**
     * Method setDebug
     * 
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }    // setDebug

    /**
     * Method isQuiet
     * 
     * @return
     */
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * Method setQuiet
     * 
     * @param quiet
     */
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    /**
     * Method isImports
     * 
     * @return
     */
    public boolean isImports() {
        return imports;
    }    // isImports

    /**
     * Method setImports
     * 
     * @param imports
     */
    public void setImports(boolean imports) {
        this.imports = imports;
    }    // setImports

    /**
     * Method isVerbose
     * 
     * @return
     */
    public boolean isVerbose() {
        return verbose;
    }    // isVerbose

    /**
     * Method setVerbose
     * 
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }    // setVerbose

    /**
     * Method isNowrap
     * 
     * @return
     */
    public boolean isNowrap() {
        return nowrap;
    }

    /**
     * Method setNowrap
     * 
     * @param nowrap
     */
    public void setNowrap(boolean nowrap) {
        this.nowrap = nowrap;
    }

    /**
     * Return the current timeout setting
     * 
     * @return
     */
    public long getTimeout() {
        return timeoutms;
    }

    /**
     * Set the timeout, in milliseconds
     * 
     * @param timeout
     */
    public void setTimeout(long timeout) {
        this.timeoutms = timeout;
    }

    /**
     * Method getUsername
     * 
     * @return
     */
    public String getUsername() {
        return username;
    }    // getUsername

    /**
     * Method setUsername
     * 
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }    // setUsername

    /**
     * Method getPassword
     * 
     * @return
     */
    public String getPassword() {
        return password;
    }    // getPassword

    /**
     * Method setPassword
     * 
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }    // setPassword

    /**
     * Method getFactory
     * 
     * @return
     */
    public GeneratorFactory getFactory() {
        return genFactory;
    }    // getFactory

    /**
     * Method setFactory
     * 
     * @param factory
     */
    public void setFactory(GeneratorFactory factory) {
        this.genFactory = factory;
    }    // setFactory

    /**
     * Get the symbol table.  The symbol table is null until
     * run is called.
     * 
     * @return
     */
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }    // getSymbolTable


    /**
     * Get the current WSDL URI.  The WSDL URI is null until
     * run is called.
     * 
     * @return
     */
    public String getWSDLURI() {

        return (symbolTable == null)
                ? null
                : symbolTable.getWSDLURI();
    }    // getWSDLURI




    public void run(WSDLDescription description)
            throws IOException, SAXException, ParserConfigurationException {
        symbolTable.populate(description);
        generate(symbolTable);
    }    // run

    /**
     * Method sanityCheck
     * 
     * @param symbolTable
     */
    protected void sanityCheck(SymbolTable symbolTable) {

        // do nothing.
    }

    /**
     * Method generate
     * 
     * @param symbolTable
     * @throws IOException
     */
    private void generate(SymbolTable symbolTable) throws IOException {

        sanityCheck(symbolTable);

        if (isDebug()) {
            symbolTable.dump(System.out);
        }

        // Generate bindings for types
        generateTypes(symbolTable);


    }    // generate

    /**
     * Generate bindings (classes and class holders) for the complex types.
     * If generating serverside (skeleton) spit out beanmappings
     * 
     * @param symbolTable
     * @throws IOException
     */
    private void generateTypes(SymbolTable symbolTable) throws IOException {

        Map elements = symbolTable.getElementIndex();
        Collection elementCollection = elements.values();
        for (Iterator i = elementCollection.iterator(); i.hasNext(); ) {
            TypeEntry type = (TypeEntry) i.next();

            // Write out the type if and only if:
            // - we found its definition (getNode())
            // - it is referenced
            // - it is not a base type or an attributeGroup
            // - it is a Type (not an Element) or a CollectionElement
            // (Note that types that are arrays are passed to getGenerator
            // because they may require a Holder)
            // A CollectionElement is an array that might need a holder
            boolean isType = ((type instanceof Type)
                    || (type instanceof CollectionElement));
            if ((type.getNode() != null)
                    && !type.getNode().getLocalName().equals("attributeGroup")
                    && isType
                    && (type.getBaseType() == null)) {
                System.out.println("Generating!!!!");
                Generator gen = genFactory.getGenerator(type, symbolTable);
                gen.generate();
            }
        }

        Map types = symbolTable.getTypeIndex();
        Collection typeCollection = types.values();
        for (Iterator i = typeCollection.iterator(); i.hasNext(); ) {
            TypeEntry type = (TypeEntry) i.next();

            // Write out the type if and only if:
            // - we found its definition (getNode())
            // - it is referenced
            // - it is not a base type or an attributeGroup
            // - it is a Type (not an Element) or a CollectionElement
            // (Note that types that are arrays are passed to getGenerator
            // because they may require a Holder)
            // A CollectionElement is an array that might need a holder
            boolean isType = ((type instanceof Type)
                    || (type instanceof CollectionElement));

            if ((type.getNode() != null)
                    && !type.getNode().getLocalName().equals("attributeGroup")
                    && isType
                    && (type.getBaseType() == null)) {
                System.out.println("Generating");
                Generator gen = genFactory.getGenerator(type, symbolTable);
                gen.generate();
            }
        }
    }    // generateTypes
}    // class Parser
