/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.wsdl.codegen;

import org.apache.axis2.wsdl.codegen.emitter.Emitter;
import org.apache.axis2.wsdl.codegen.extension.CodeGenExtension;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CodeGenerationEngine {

    private static final Log log = LogFactory.getLog(CodeGenerationEngine.class);

    /** Array List for pre-extensions. Extensions that run before the emitter */
    private List preExtensions = new ArrayList();
    /** Array List for post-extensions. Extensions that run after the codegens */
    private List postExtensions = new ArrayList();

    /** Codegen configuration  reference */
    private CodeGenConfiguration configuration;

    /**
     * @param configuration
     * @throws CodeGenerationException
     */
    public CodeGenerationEngine(CodeGenConfiguration configuration) throws CodeGenerationException {
        this.configuration = configuration;
        loadExtensions();
    }

    /**
     * Loads the relevant preExtensions
     *
     * @throws CodeGenerationException
     */
    private void loadExtensions() throws CodeGenerationException {
        //load pre extensions
        String[] extensions = ConfigPropertyFileLoader.getExtensionClassNames();
        if (extensions != null) {
            for (int i = 0; i < extensions.length; i++) {
                //load the Extension class
                addPreExtension((CodeGenExtension)getObjectFromClassName(extensions[i].trim()));
            }
        }

        //load post extensions
        String[] postExtensions = ConfigPropertyFileLoader.getPostExtensionClassNames();
        if (postExtensions != null) {
            for (int i = 0; i < postExtensions.length; i++) {
                //load the Extension class
                addPostExtension(
                        (CodeGenExtension)getObjectFromClassName(postExtensions[i].trim()));
            }
        }

    }

    /**
     * Adds a given extension to the list
     *
     * @param ext
     */
    private void addPreExtension(CodeGenExtension ext) {
        if (ext != null) {
            preExtensions.add(ext);
        }
    }

    /**
     * Adds a given extension to the list
     *
     * @param ext
     */
    private void addPostExtension(CodeGenExtension ext) {
        if (ext != null) {
            postExtensions.add(ext);
        }
    }

    /**
     * Generate the code!!
     *
     * @throws CodeGenerationException
     */
    public void generate() throws CodeGenerationException {
        try {
            //engage the pre-extensions
            for (int i = 0; i < preExtensions.size(); i++) {
                ((CodeGenExtension)preExtensions.get(i)).engage(configuration);
            }

            Emitter emitter;


            TypeMapper mapper = configuration.getTypeMapper();
            if (mapper == null) {
                // this check is redundant here. The default databinding extension should
                // have already figured this out and thrown an error message. However in case the
                // users decides to mess with the config it is safe to keep this check in order to throw
                // a meaningful error message
                throw new CodeGenerationException(
                        CodegenMessages.getMessage("engine.noProperDatabindingException"));
            }

            //Find and invoke the emitter by reflection
            Map emitterMap = ConfigPropertyFileLoader.getLanguageEmitterMap();
            String className = (String)emitterMap.get(configuration.getOutputLanguage());
            if (className != null) {
                emitter = (Emitter)getObjectFromClassName(className);
                emitter.setCodeGenConfiguration(configuration);
                emitter.setMapper(mapper);
            } else {
                throw new Exception(CodegenMessages.getMessage("engine.emitterMissing"));
            }

            //invoke the necessary methods in the emitter
            if (configuration.isServerSide()) {
                emitter.emitSkeleton();
                // if the users want both client and server, it would be in the
                // generate all option
                if (configuration.isGenerateAll()) {
                    emitter.emitStub();
                }
            } else {
                emitter.emitStub();
            }

            //engage the post-extensions
            for (int i = 0; i < postExtensions.size(); i++) {
                ((CodeGenExtension)postExtensions.get(i)).engage(configuration);
            }

        } catch (ClassCastException e) {
            throw new CodeGenerationException(CodegenMessages.getMessage("engine.wrongEmitter"), e);
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }


    }

    /**
     * gets a object from the class
     *
     * @param className
     */
    private Object getObjectFromClassName(String className) throws CodeGenerationException {
        try {
            Class extensionClass = getClass().getClassLoader().loadClass(className);
            return extensionClass.newInstance();
        } catch (ClassNotFoundException e) {
            // TODO REVIEW FOR JAVA 6
            // In Java 5, if you passed an array string such as "[Lcom.mypackage.MyClass;" to
            // loadClass, the class would indeed be loaded.  
            // In JDK6, a ClassNotFoundException is thrown. 
            // The work-around is to use code Class.forName instead.
            // Example:
            // try {
            //       classLoader.loadClass(name);
            //  } catch (ClassNotFoundException e) {
            //       Class.forName(name, false, loader);
            //  }
            log.debug(CodegenMessages.getMessage("engine.extensionLoadProblem"), e);
            return null;
        } catch (InstantiationException e) {
            throw new CodeGenerationException(
                    CodegenMessages.getMessage("engine.extensionInstantiationProblem"), e);
        } catch (IllegalAccessException e) {
            throw new CodeGenerationException(CodegenMessages.getMessage("engine.illegalExtension"),
                                              e);
        } catch (NoClassDefFoundError e) {
            log.debug(CodegenMessages.getMessage("engine.extensionLoadProblem"), e);
            return null;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }

    }

    /**
     * calculates the URI
     * needs improvement
     *
     * @param currentURI
     */
    private String getURI(String currentURI) throws URISyntaxException, IOException {

        File file = new File(currentURI);
        if (file.exists()){
            return file.getCanonicalFile().toURI().toString();
        } else {
            return currentURI;
        }

    }

    public CodeGenConfiguration getConfiguration() {
        return configuration;
    }
}
