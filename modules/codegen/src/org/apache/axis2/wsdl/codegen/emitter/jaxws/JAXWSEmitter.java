package org.apache.axis2.wsdl.codegen.emitter.jaxws;

import org.w3c.dom.Document;
import org.apache.axis2.wsdl.codegen.writer.FileWriter;
import org.apache.axis2.wsdl.codegen.writer.SkeletonInterfaceWriter;
import org.apache.axis2.wsdl.codegen.writer.ExceptionWriter;
import org.apache.axis2.wsdl.codegen.writer.SkeletonWriter;
import org.apache.axis2.wsdl.codegen.emitter.AxisServiceBasedMultiLanguageEmitter;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.util.XSLTIncludeResolver;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public abstract class JAXWSEmitter extends AxisServiceBasedMultiLanguageEmitter {

    protected final static String TYPE_SUFFIX = "Type";
    protected final static String SERVICE_SUFFIX = "Service";
    protected final static String EXCEPTION_SUFFIX = "Exception";

    protected String portTypeName;
    protected String serviceName;
    protected String packageName;
    protected String targetNS;

    public void setCodeGenConfiguration(CodeGenConfiguration configuration) {
        super.setCodeGenConfiguration(configuration);
        portTypeName = (String) axisService.getParameterValue(WSDL2Constants.INTERFACE_LOCAL_NAME);
        serviceName = axisService.getName();
        targetNS = codeGenConfiguration.getTargetNamespace();
        packageName = codeGenConfiguration.getPackageName();
    }

    public void emitSkeleton() throws CodeGenerationException {

        try {

            Map originalMap = getNewCopy(this.mapper.getAllMappedNames());
            // we are going to generate following files seperately per service
            for (Iterator axisServicesIter = this.axisServices.iterator();
                 axisServicesIter.hasNext();) {
                this.axisService = (AxisService) axisServicesIter.next();
                this.axisBinding =
                        axisService.getEndpoint(axisService.getEndpointName()).getBinding();

                // see the comment at updateMapperClassnames for details and reasons for
                // calling this method
                if (mapper.isObjectMappingPresent()) {
                    copyMap(originalMap, this.mapper.getAllMappedNames());
                    updateMapperForMessageReceiver();
                } else {
                    copyToFaultMap();
                }

                //handle faults
                generateAndPopulateFaultNames();

                //write the Service Endpoint Interface
                writeServiceEndpointInterface();

                //write the Exceptions
                writeExceptions();

                //write the Service Class
                writeServiceClass();
            }

            // save back type map
            if (this.mapper.isObjectMappingPresent()) {
                copyMap(originalMap, this.mapper.getAllMappedNames());
            }

            // write service xml
            // if asked
            if (codeGenConfiguration.isGenerateDeployementDescriptor()) {
                writeServiceXml();
            }

            //write the ant build
            //we skip this for the flattened case
            if (!codeGenConfiguration.isFlattenFiles()) {
                writeAntBuild();
            }


        } catch (CodeGenerationException cgExp) {
            throw cgExp;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    /**
     * Write the service endpoint interface
     *
     * @throws Exception
     */
    protected void writeServiceEndpointInterface() throws Exception {

        Document skeletonModel = createDOMDocumentForSEI();
        debugLogDocument("Document for Service Endpoint Interface:", skeletonModel);
        FileWriter skeletonInterfaceWriter = new SkeletonInterfaceWriter(
                codeGenConfiguration.isFlattenFiles() ?
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                codeGenConfiguration.getSourceLocation())
                , this.codeGenConfiguration.getOutputLanguage());

        writeFile(skeletonModel, skeletonInterfaceWriter);
    }

    /**
     * Writes the exception calsses.
     */
    protected void writeExceptions() throws Exception {
        String key;
        Iterator iterator = fullyQualifiedFaultClassNameMap.keySet().iterator();
        while (iterator.hasNext()) {
            key = (String) iterator.next();


            Document skeletonModel = createDOMDocumentForException(key);
            debugLogDocument("Document for Exception Class:", skeletonModel);
            ExceptionWriter exceptionWriter =
                    new ExceptionWriter(
                            codeGenConfiguration.isFlattenFiles() ?
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                            null) :
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                            codeGenConfiguration.getSourceLocation()),
                            codeGenConfiguration.getOutputLanguage());

            writeFile(skeletonModel, exceptionWriter);
        }
    }

    /**
     * Write the service class
     *
     * @throws Exception
     */
    protected void writeServiceClass() throws Exception {
        Document skeletonModel = createDOMDocumentForServiceClass();
        debugLogDocument("Document for Service Endpoint Interface:", skeletonModel);
        FileWriter skeletonInterfaceWriter = new SkeletonWriter(
                codeGenConfiguration.isFlattenFiles() ?
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                codeGenConfiguration.getSourceLocation())
                , this.codeGenConfiguration.getOutputLanguage());

        writeFile(skeletonModel, skeletonInterfaceWriter);
    }

    /**
     * Creates the XML model for the Service Endpoint interface
     *
     * @return DOM Document
     */
    protected abstract Document createDOMDocumentForSEI();

    /**
     * Creates the XML model for the Service Class
     *
     * @return DOM Document
     */
    protected abstract Document createDOMDocumentForServiceClass();

    /**
     * Creates the XML model for a Exception Class
     *
     * @param key String
     * @return DOM Document
     */
    protected abstract Document createDOMDocumentForException(String key);

    //Util methods
    public String extratClassName(String fullyQualifiedName) {
        if (fullyQualifiedName == null) {
            return "";
        }

        String className = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.'),
                fullyQualifiedName.length());

        if (className.charAt(0) == '.') {
            return className.substring(1);
        }

        return className;
    }

    protected String getFullyQualifiedName(String className, String packageName) {
//        className = makeJavaClassName(className);
        return packageName + "." + className;
    }

    protected String resolveNameCollision(String className, String packageName, String suffix) {
        className = makeJavaClassName(className);
        String fullQualifiedName = getFullyQualifiedName(className, packageName);
        Map map = mapper.getAllMappedNames();
        if (map.containsValue(fullQualifiedName)) {
            return className + suffix;
        }
        return className;
    }
}
