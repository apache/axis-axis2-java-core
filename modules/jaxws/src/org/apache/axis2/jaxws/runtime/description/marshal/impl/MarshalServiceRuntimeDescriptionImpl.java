package org.apache.axis2.jaxws.runtime.description.marshal.impl;

import java.util.TreeSet;

import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;


public class MarshalServiceRuntimeDescriptionImpl implements
        MarshalServiceRuntimeDescription {

    private ServiceDescription serviceDesc;
    private String key; 
    private TreeSet<String> packages;
    
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
}
