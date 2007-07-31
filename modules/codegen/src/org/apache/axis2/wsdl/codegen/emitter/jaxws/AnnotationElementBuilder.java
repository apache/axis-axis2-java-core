package org.apache.axis2.wsdl.codegen.emitter.jaxws;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.apache.axis2.util.XSLTUtils;

public class AnnotationElementBuilder {

    static Element buildWebServiceAnnotationElement(String name, String targetNS, String wsdlLocation,
                                                    Document doc) {

        Element annotationElement = doc.createElement("annotation");
        XSLTUtils.addAttribute(doc, "name", "javax.jws.WebService", annotationElement);

        Element paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "name", paramElement);
        XSLTUtils.addAttribute(doc, "value", name, paramElement);
        annotationElement.appendChild(paramElement);

        paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "targetNamespace", paramElement);
        XSLTUtils.addAttribute(doc, "value", targetNS, paramElement);
        annotationElement.appendChild(paramElement);

        return annotationElement;
    }

    static Element buildWebFaultAnnotationElement(String name, String targetNS, Document doc) {
        Element annotationElement = doc.createElement("annotation");
        XSLTUtils.addAttribute(doc, "name", "javax.xml.ws.WebFault", annotationElement);

        Element paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "name", paramElement);
        XSLTUtils.addAttribute(doc, "value", name, paramElement);
        annotationElement.appendChild(paramElement);

        paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "targetNamespace", paramElement);
        XSLTUtils.addAttribute(doc, "value", targetNS, paramElement);
        annotationElement.appendChild(paramElement);

        return annotationElement;
    }

    static Element buildWebServiceClientAnnotationElement(String name, String targetNS, String wsdlLocation,
                                                          Document doc) {

        Element annotationElement = doc.createElement("annotation");
        XSLTUtils.addAttribute(doc, "name", "javax.xml.ws.WebServiceClient", annotationElement);

        Element paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "name", paramElement);
        XSLTUtils.addAttribute(doc, "value", name, paramElement);
        annotationElement.appendChild(paramElement);

        paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "targetNamespace", paramElement);
        XSLTUtils.addAttribute(doc, "value", targetNS, paramElement);
        annotationElement.appendChild(paramElement);

        paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "wsdlLocation", paramElement);
        XSLTUtils.addAttribute(doc, "value", wsdlLocation, paramElement);
        annotationElement.appendChild(paramElement);

        return annotationElement;
    }

    static Element buildWebEndPointAnnotationElement(String name, Document doc) {
        Element annotationElement = doc.createElement("annotation");
        XSLTUtils.addAttribute(doc, "name", "javax.xml.ws.WebEndpoint", annotationElement);

        Element paramElement = doc.createElement("param");
        XSLTUtils.addAttribute(doc, "type", "name", paramElement);
        XSLTUtils.addAttribute(doc, "value", name, paramElement);
        annotationElement.appendChild(paramElement);

        return annotationElement;
    }
}

