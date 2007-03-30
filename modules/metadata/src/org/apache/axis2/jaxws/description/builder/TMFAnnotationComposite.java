package org.apache.axis2.jaxws.description.builder;

/**
 * This interface will be implemented by the DescriptionBuilderComposite,
 * MethodDescriptionComposite, and FieldDescriptionCompoiste. It will declare setters for annotation
 * types that are common to Types, Methods, and Fields.
 */
public interface TMFAnnotationComposite {

    public void setHandlerChainAnnot(HandlerChainAnnot hcAnnot);

    public void setWebServiceRefAnnot(WebServiceRefAnnot wsrAnnot);
}
