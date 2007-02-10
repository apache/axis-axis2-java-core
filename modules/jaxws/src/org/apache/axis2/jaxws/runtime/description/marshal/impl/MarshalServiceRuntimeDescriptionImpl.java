package org.apache.axis2.jaxws.runtime.description.marshal.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.runtime.description.marshal.AnnotationDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;


public class MarshalServiceRuntimeDescriptionImpl implements
        MarshalServiceRuntimeDescription {

    private ServiceDescription serviceDesc;
    private String key; 
    private TreeSet<String> packages;
    private Map<String, AnnotationDesc> annotationMap = null;
    
    protected MarshalServiceRuntimeDescriptionImpl(String key,
                ServiceDescription serviceDesc) {
        this.serviceDesc = serviceDesc;
        this.key = key;
    }


    public ServiceDescription getServiceDescription() {
        return serviceDesc;
    }

    public String getKey() {
        return key;
    }

    public TreeSet<String> getPackages() {
        return packages;
    }

    void setPackages(TreeSet<String> packages) {
        this.packages = packages;
    }

    public AnnotationDesc getAnnotationDesc(Class cls) {
        String className = cls.getCanonicalName();
        AnnotationDesc aDesc = annotationMap.get(className);
        if (aDesc != null) {
            // Cache hit
            return aDesc;
        }
        // Cache miss...we cannot update the map because we don't want to introduce a sync call.
        aDesc = AnnotationDescImpl.create(cls);
        
        return aDesc;
    }
    
    
    void setAnnotationMap(Map<String, AnnotationDesc> map) {
        this.annotationMap = map;
    }
    
}
