package org.apache.axis2.wsdl.codegen.emitter.jaxws;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.util.JavaUtils;
import org.apache.woden.internal.util.dom.DOM2Writer;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class JAXWS20Emitter extends JAXWSEmitter {

    /**
     * Creates the XML model for the Service Endpoint interface
     *
     * @return DOM Document
     */
    protected Document createDOMDocumentForSEI() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("javaConstruct");

        Element importList = doc.createElement("importList");
        rootElement.appendChild(importList);

        portTypeName = resolveNameCollision(portTypeName, packageName, TYPE_SUFFIX);

        addAttribute(doc, "package", packageName, rootElement);
        addAttribute(doc, "targetNamespace", targetNS, rootElement);
        addAttribute(doc, "name", portTypeName, rootElement);

        Element annotationElement = AnnotationElementBuilder.buildWebServiceAnnotationElement(portTypeName, targetNS,
                "", doc);
        rootElement.appendChild(annotationElement);

        loadOperations(doc, rootElement, null);

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));
        doc.appendChild(rootElement);
        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////////////////
        return doc;

    }

    /**
     * Creates the XML model for the Service Class
     *
     * @return DOM Document
     */
    protected Document createDOMDocumentForServiceClass() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("javaConstruct");

        Element importList = doc.createElement("importList");
        rootElement.appendChild(importList);

        String capitalizedServiceName = serviceName.toUpperCase();
        String wsdlLocation = "Needs to be fixed";

        serviceName = resolveNameCollision(serviceName, packageName, TYPE_SUFFIX);

        addAttribute(doc, "package", packageName, rootElement);
        addAttribute(doc, "targetNamespace", targetNS, rootElement);
        addAttribute(doc, "name", serviceName, rootElement);
        addAttribute(doc, "wsdlLocation", wsdlLocation, rootElement);
        addAttribute(doc, "capitalizedServiceName", capitalizedServiceName, rootElement);

        //Adding annotations -- tempory solution  hardcoded solution
//        Element importElement;
//        importElement = doc.createElement("import");
//        addAttribute(doc, "value", "java.net.URL", importElement);
//        importList.appendChild(importElement);

        Element annotationElement = AnnotationElementBuilder.buildWebServiceClientAnnotationElement(serviceName,
                targetNS, wsdlLocation, doc);
        rootElement.appendChild(annotationElement);

        //Building portType Elements -- think of a suitable solution
        for (Iterator portIterator = axisService.getEndpoints().keySet().iterator(); portIterator.hasNext();) {
            String portName = (String) portIterator.next();

            Element portElement = doc.createElement("port");
            addAttribute(doc, "portName", portName, portElement);
            addAttribute(doc, "portTypeName", portTypeName, portElement);

            Element endPointAnnoElement = AnnotationElementBuilder.buildWebEndPointAnnotationElement(portName, doc);
            portElement.appendChild(endPointAnnoElement);

            rootElement.appendChild(portElement);
        }

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));
        doc.appendChild(rootElement);
        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////////////////
        return doc;
    }

    /**
     * Creates the XML model for a Exception Class
     *
     * @param key String
     * @return DOM Document
     */
    protected Document createDOMDocumentForException(String key) {
        Document doc = getEmptyDocument();
        Element faultElement;

        faultElement = doc.createElement("javaConstruct");
        Element importList = doc.createElement("importList");
        faultElement.appendChild(importList);

        addAttribute(doc, "package", packageName, faultElement);
        addAttribute(doc, "targetNamespace", targetNS, faultElement);

        String exceptionClassName = (String) faultClassNameMap.get(key);
        exceptionClassName = resolveNameCollision(exceptionClassName, packageName, EXCEPTION_SUFFIX);
        addAttribute(doc, "name",exceptionClassName, faultElement);
//            addAttribute(doc, "shortName",
//                    (String) faultClassNameMap.get(key) + "Exception",
//                    faultElement);

        //the type represents the type that will be wrapped by this
        //name
        String typeMapping =
                this.mapper.getTypeMappingName((QName) faultElementQNameMap.get(key));
        String shortType = extratClassName(typeMapping);

        addAttribute(doc, "type", (typeMapping == null)
                ? ""
                : typeMapping, faultElement);

        addAttribute(doc, "shortType", (shortType == null)
                ? ""
                : shortType, faultElement);

        Element importElement;
        importElement = doc.createElement("import");
        addAttribute(doc, "value", typeMapping, importElement);
        importList.appendChild(importElement);

//            String attribValue = (String) instantiatableMessageClassNames.
//                    get(key);
//            addAttribute(doc, "instantiatableType",
//                    attribValue == null ? "" : attribValue,
//                    faultElement);

        // add an extra attribute to say whether the type mapping is
        // the default
        if (mapper.getDefaultMappingName().equals(typeMapping)) {
            addAttribute(doc, "default", "yes", faultElement);
        }

        addAttribute(doc, "value", getParamInitializer(typeMapping),
                faultElement);

        Element annotationElement = AnnotationElementBuilder.buildWebFaultAnnotationElement(typeMapping,
                codeGenConfiguration.getTargetNamespace(), doc);
        faultElement.appendChild(annotationElement);
        doc.appendChild(faultElement);
        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(faultElement));
        ////////////////////////////////////////////////////////////
        return doc;
    }
}