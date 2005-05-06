package org.apache.axis.wsdl.tojava;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.WSDLException;

import org.apache.axis.wsdl.builder.WOMBuilderFactory;
import org.apache.axis.wsdl.tojava.emitter.ClientJavaEmitter;
import org.apache.axis.wsdl.tojava.emitter.Emitter;
import org.apache.axis.wsdl.tojava.extension.AxisBindingBuilder;
import org.apache.axis.wsdl.tojava.extension.CodeGenExtention;
import org.apache.axis.wsdl.tojava.xslt.CSharpEmitter;
import org.apache.axis.wsdl.tojava.xslt.JavaEmitter;
import org.apache.axis.wsdl.tojava.xslt.XSLTConstants;
import org.apache.wsdl.WSDLDescription;

/**
 * @author chathura@opensource.lk
 *
 */
public class CodeGenerationEngine {

    private List moduleEndpoints = new ArrayList();

    private CodeGenConfiguration configuration;




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

    }


    public void generate()throws CodeGenerationException{
        for(int i = 0; i< this.moduleEndpoints.size(); i++){
            ((CodeGenExtention)this.moduleEndpoints.get(i)).engage();
        }

        Emitter clientEmitter = null;

        switch (configuration.getOutputLanguage()){
            case XSLTConstants.LanguageTypes.JAVA:
                if (configuration.isAdvancedCodeGenEnabled()){
                    clientEmitter =  new JavaEmitter(this.configuration);
                } else{
                    clientEmitter =  new ClientJavaEmitter(this.configuration);
                }
                break;

            case XSLTConstants.LanguageTypes.C_SHARP:
                clientEmitter = new CSharpEmitter(this.configuration);
            case XSLTConstants.LanguageTypes.C_PLUS_PLUS:
            case XSLTConstants.LanguageTypes.VB_DOT_NET:

            default:
                throw new UnsupportedOperationException();

        }

        clientEmitter.emitStub();


    }


    private WSDLDescription getWOM(CommandLineOptionParser parser) throws WSDLException, IOException {
        String uri = ((CommandLineOption) parser.getAllOptions().get(CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION)).getOptionValue();
        InputStream in = new FileInputStream(new File(uri));
        return WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11).build(in);
    }



}
