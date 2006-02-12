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

package org.apache.axis2.wsdl.codegen;

import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.axis2.wsdl.codegen.emitter.Emitter;
import org.apache.axis2.wsdl.codegen.extension.CodeGenExtension;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.CommandLineOption;
import org.apache.axis2.wsdl.util.CommandLineOptionConstants;
import org.apache.axis2.wsdl.util.CommandLineOptionParser;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLDescription;

import javax.wsdl.WSDLException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CodeGenerationEngine {
    private Log log = LogFactory.getLog(getClass());

    private List extensions = new ArrayList();


    private CodeGenConfiguration configuration;

    /**
     * 
     * @param configuration
     * @throws CodeGenerationException
     */
    public CodeGenerationEngine(CodeGenConfiguration configuration) throws CodeGenerationException {
        this.configuration = configuration;
        loadExtensions();
    }

    /**
     *
     * @param parser
     * @throws CodeGenerationException
     */
    public CodeGenerationEngine(CommandLineOptionParser parser) throws CodeGenerationException {
        WSDLDescription wom;
        Map allOptions = parser.getAllOptions();
        String wsdlUri;
        try {

            CommandLineOption option = (CommandLineOption)allOptions.get(CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION);
            wsdlUri = option.getOptionValue();
            wom = this.getWOM(wsdlUri);
        } catch (WSDLException e) {
            throw new CodeGenerationException(CodegenMessages.getMessage("engine.wsdlParsingException"), e);
        }

        configuration = new CodeGenConfiguration(wom, allOptions);
        configuration.setBaseURI(getBaseURI(wsdlUri));
        loadExtensions();
    }

    private void loadExtensions() throws CodeGenerationException {

        String[] extensions = ConfigPropertyFileLoader.getExtensionClassNames();
        for (int i = 0; i < extensions.length; i++) {
            //load the Extension class
            addExtension((CodeGenExtension) getObjectFromClassName(extensions[i]));
        }

    }

    private void addExtension(CodeGenExtension ext) {
        if(ext != null) {
            ext.init(configuration);
            extensions.add(ext);
        }
    }


    public void generate() throws CodeGenerationException {
        try {
            for (int i = 0; i < extensions.size(); i++) {
                ((CodeGenExtension) extensions.get(i)).engage();
            }

            Emitter emitter;


            TypeMapper mapper = configuration.getTypeMapper();
            if (mapper == null) {
                // this check is redundant here. The default databinding extension should
                // have already figured this out and thrown an error message. However in case the
                // users decides to mess with the config it is safe to keep this check in order to throw
                // a meaningful error message
                throw new CodeGenerationException(CodegenMessages.getMessage("engine.noProperDatabindingException"));
            }

            Map emitterMap = ConfigPropertyFileLoader.getLanguageEmitterMap();
            String className = (String)emitterMap.get(configuration.getOutputLanguage());
            if (className != null) {
                emitter = (Emitter) getObjectFromClassName(className);
                emitter.setCodeGenConfiguration(configuration);
                emitter.setMapper(mapper);
            } else {
                throw new Exception(CodegenMessages.getMessage("engine.emitterMissing"));
            }



            if (configuration.isServerSide()) {
                emitter.emitSkeleton();
                //if the users want both client and server, it would be in the 
                // generate all option

                if (configuration.isGenerateAll()) {
                    emitter.emitStub();
                }
            }else{
                emitter.emitStub();
            }



        } catch (ClassCastException e) {
            throw new CodeGenerationException(CodegenMessages.getMessage("engine.wrongEmitter"), e);

        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }


    }


    /**
     *
     * @param uri
     * @return
     * @throws WSDLException
     */
    private WSDLDescription getWOM(String uri) throws WSDLException {
        //assume that the builder is always WSDL 1.1 - later we'll have to edit this to allow
        //WSDL version to be passed
        return WOMBuilderFactory.getBuilder(org.apache.wsdl.WSDLConstants.WSDL_1_1).build(uri)
                .getDescription();
    }


    /**
     * gets a object from the class
     *
     * @param className
     * @return
     */
    private Object getObjectFromClassName(String className) throws CodeGenerationException {
        try {
            Class extensionClass = getClass().getClassLoader().loadClass(className);
            return extensionClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new CodeGenerationException(CodegenMessages.getMessage("engine.extensionLoadProblem"), e);
        } catch (InstantiationException e) {
            throw new CodeGenerationException(CodegenMessages.getMessage("engine.extensionInstantiationProblem"), e);
        } catch (IllegalAccessException e) {
            throw new CodeGenerationException(CodegenMessages.getMessage("engine.illegalExtension"), e);
        } catch (NoClassDefFoundError e) {
            log.debug(CodegenMessages.getMessage("engine.extensionLoadProblem"), e);
            return null;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }

    }

    /**
     * calculates the base URI
     * Needs improvement but works fine for now ;)
     * @param currentURI
     * @return
     */
    private String getBaseURI(String currentURI){
        String baseURI= null;
        if (!currentURI.startsWith("http://")) {
            // the uri should be a file
            try {
                currentURI = new File(currentURI).toURL().toString();
            } catch (MalformedURLException e) {
                throw new RuntimeException("Cannot find baseuri for :" + currentURI);
            }
        }
        String uriFrag = currentURI.substring(0, currentURI.lastIndexOf("/"));
        baseURI = uriFrag + (uriFrag.endsWith("/") ? "" : "/");
        return baseURI;
    }
}
