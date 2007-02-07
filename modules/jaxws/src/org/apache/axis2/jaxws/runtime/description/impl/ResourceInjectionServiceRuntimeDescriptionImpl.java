package org.apache.axis2.jaxws.runtime.description.impl;

import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.runtime.description.ResourceInjectionServiceRuntimeDescription;

public class ResourceInjectionServiceRuntimeDescriptionImpl implements
        ResourceInjectionServiceRuntimeDescription {

    private ServiceDescription serviceDesc;
    private String key; 
    private boolean _hasResourceAnnotation;
    
    protected ResourceInjectionServiceRuntimeDescriptionImpl(String key,
                ServiceDescription serviceDesc) {
        this.serviceDesc = serviceDesc;
        this.key = key;
    }

    public boolean hasResourceAnnotation() {
        return _hasResourceAnnotation;
    }

    public ServiceDescription getServiceDescription() {
        return serviceDesc;
    }

    public String getKey() {
        return key;
    }

    /** Called by Builder code
     * @param value
     */
    void setResourceAnnotation(boolean value) {
        _hasResourceAnnotation = value;
    }
}
