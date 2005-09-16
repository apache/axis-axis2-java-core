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
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;
import org.apache.wsdl.WSDLDescription;

import javax.wsdl.WSDLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CodeGenerationEngine {
    private List moduleEndpoints = new ArrayList();


    private CodeGenConfiguration configuration;

    public CodeGenerationEngine(CodeGenConfiguration config) throws CodeGenerationException{
        this.configuration = config;
        loadExtensions();
    }

    public CodeGenerationEngine(CommandLineOptionParser parser) throws CodeGenerationException {
        WSDLDescription wom;
        try {
            wom = this.getWOM(parser);
        } catch (WSDLException e) {
            throw new CodeGenerationException("Error parsing WSDL", e);
        } catch (IOException e1) {
            throw new CodeGenerationException("Invalid WSDL Location ", e1);
        }

        this.configuration = new CodeGenConfiguration(wom, parser);
        loadExtensions();
    }

    private void loadExtensions() throws CodeGenerationException{

        String[] extensions = ConfigPropertyFileLoader.getExtensionClassNames();
        for (int i = 0; i < extensions.length; i++) {
            //load the Extension class
            addExtension((CodeGenExtension)getObjectFromClassName(extensions[i]));

        }

    }

    private void addExtension(CodeGenExtension ext){
        ext.init(this.configuration);
        this.moduleEndpoints.add(ext);
    }
    public void generate() throws CodeGenerationException {
        try {
            for (int i = 0; i < this.moduleEndpoints.size(); i++) {
                ((CodeGenExtension) this.moduleEndpoints.get(i)).engage();
            }

            Emitter emitter;
            TypeMapper mapper = configuration.getTypeMapper();

            Map emitterMap = ConfigPropertyFileLoader.getLanguageEmitterMap();
            String className = emitterMap.get(this.configuration.getOutputLanguage()).toString();
            if (className!=null){
                
                emitter = (Emitter)getObjectFromClassName(className);
                emitter.setCodeGenConfiguration(this.configuration);
                emitter.setMapper(mapper);

            }else{
                throw new Exception("Emitter class not found!");
            }


            if (this.configuration.isServerSide()){
                emitter.emitSkeleton();
            }else{
                emitter.emitStub();
            }

        } catch (ClassCastException e) {
            throw new CodeGenerationException("Non emitter class found!",e);

        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }


    }


    private WSDLDescription getWOM(CommandLineOptionParser parser) throws WSDLException,
            IOException {
        String uri = ((CommandLineOption) parser.getAllOptions().get(
                CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION)).getOptionValue();
        return WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11).build(uri)
                .getDescription();
    }


    /**
     * gets a object from the class
     * @param className
     * @return
     */
    private Object getObjectFromClassName(String className) throws CodeGenerationException{
        try {
            Class extensionClass = this.getClass().getClassLoader().loadClass(className);
            return extensionClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new CodeGenerationException("Extension class loading problem",e);
        } catch (InstantiationException e) {
            throw new CodeGenerationException("Extension class instantiation problem",e);
        } catch (IllegalAccessException e) {
            throw new CodeGenerationException("Illegal extension!",e);
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }

    }
}
