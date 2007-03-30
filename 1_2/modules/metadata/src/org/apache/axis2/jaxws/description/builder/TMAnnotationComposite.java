package org.apache.axis2.jaxws.description.builder;

/**
 * This interface will be implemented by the DescriptionBuilderComposite and
 * MethodDescriptionComposite. It will declare setters for annotation types that are common to Types
 * and Methods.
 */
public interface TMAnnotationComposite {

    public void setSoapBindingAnnot(SoapBindingAnnot sbAnnot);

}
