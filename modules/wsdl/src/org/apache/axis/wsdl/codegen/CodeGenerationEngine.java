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

package org.apache.axis.wsdl.codegen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.WSDLException;

import org.apache.axis.wsdl.builder.WOMBuilderFactory;
import org.apache.axis.wsdl.codegen.emitter.CSharpEmitter;
import org.apache.axis.wsdl.codegen.emitter.Emitter;
import org.apache.axis.wsdl.codegen.emitter.JavaEmitter;
import org.apache.axis.wsdl.codegen.extension.AxisBindingBuilder;
import org.apache.axis.wsdl.codegen.extension.CodeGenExtension;
import org.apache.axis.wsdl.codegen.extension.PackageFinder;
import org.apache.axis.wsdl.codegen.extension.XMLBeansExtension;
import org.apache.axis.wsdl.databinding.TypeMapper;
import org.apache.wsdl.WSDLDescription;

/**
 * @author chathura@opensource.lk
 *
 */
public class CodeGenerationEngine {

    private List moduleEndpoints = new ArrayList();

    private CodeGenConfiguration configuration;


    public CodeGenerationEngine(CodeGenConfiguration config){
        this.configuration = config;
        AxisBindingBuilder axisBindingBuilder = new AxisBindingBuilder();
        axisBindingBuilder.init(this.configuration);
        axisBindingBuilder.engage();
    }

    public CodeGenerationEngine(CommandLineOptionParser parser) throws CodeGenerationException{
        WSDLDescription wom ;
        try {
            wom = this.getWOM(parser);
        }
        catch (WSDLException e) {
            throw new CodeGenerationException("Error parsing WSDL", e);
        }
        catch(IOException e1){
            throw new CodeGenerationException("Invalid WSDL Location ", e1);
        }

        this.configuration = new CodeGenConfiguration(wom, parser);
        AxisBindingBuilder axisBindingBuilder = new AxisBindingBuilder();
        axisBindingBuilder.init(this.configuration);
        axisBindingBuilder.engage();

        PackageFinder packageFinder = new PackageFinder();
        packageFinder.init(this.configuration);
        this.moduleEndpoints.add(packageFinder);

        XMLBeansExtension xmlBeanExtension = new XMLBeansExtension();
        xmlBeanExtension.init(this.configuration);
        this.moduleEndpoints.add(xmlBeanExtension);
    }


    public void generate()throws CodeGenerationException{

        for(int i = 0; i< this.moduleEndpoints.size(); i++){
            ((CodeGenExtension)this.moduleEndpoints.get(i)).engage();
        }

        Emitter emitter = null;
        TypeMapper mapper = configuration.getTypeMapper();

        switch (configuration.getOutputLanguage()){
            case XSLTConstants.LanguageTypes.JAVA:
                emitter =  new JavaEmitter(this.configuration,mapper);
                break;
            case XSLTConstants.LanguageTypes.C_SHARP:
                emitter = new CSharpEmitter(this.configuration,mapper);
                break;
            case XSLTConstants.LanguageTypes.C_PLUS_PLUS:
            case XSLTConstants.LanguageTypes.VB_DOT_NET:

            default:
                throw new UnsupportedOperationException();

        }
        if (this.configuration.isServerSide())
            emitter.emitSkeleton();
        else
            emitter.emitStub();


    }


    private WSDLDescription getWOM(CommandLineOptionParser parser) throws WSDLException, IOException {
        String uri = ((CommandLineOption) parser.getAllOptions().get(CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION)).getOptionValue();
        InputStream in = new FileInputStream(new File(uri));
        return WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11).build(in);
    }



}
