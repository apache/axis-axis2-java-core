/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.runtime.description.marshal.impl;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.runtime.description.marshal.AnnotationDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.FaultBeanDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.axis2.jaxws.utility.PropertyDescriptorPlus;
import org.apache.axis2.jaxws.utility.XMLRootElementUtil;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;


/**
 * @author scheu
 *
 */
/**
 * @author scheu
 *
 */
public class MarshalServiceRuntimeDescriptionImpl implements
        MarshalServiceRuntimeDescription {

    private ServiceDescription serviceDesc;
    private String key;
    private TreeSet<String> packages;
    private String packagesKey;
    private Map<String, AnnotationDesc> annotationMap = null;
    private Map<Class, Map<String, PropertyDescriptorPlus>> pdMapCache = null;
    private Map<OperationDescription, String> requestWrapperMap = null;
    private Map<OperationDescription, String> responseWrapperMap = null;
    private Map<FaultDescription, FaultBeanDesc> faultBeanDescMap = null;
    private Map<OperationDescription, Method> methodMap = null;
    private MessageFactory messageFactory =
            (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);


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

    public String getPackagesKey() {
        return packagesKey;
    }

    void setPackages(TreeSet<String> packages) {
        this.packages = packages;
        this.packagesKey = packages.toString();  // Unique key for searches
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


    public Map<String, PropertyDescriptorPlus> getPropertyDescriptorMap(Class cls) {
        // We are caching by class.  
        Map<String, PropertyDescriptorPlus> pdMap = pdMapCache.get(cls);
        if (pdMap != null) {
            // Cache hit
            return pdMap;
        }

        // Cache miss...this can occur if the classloader changed.
        // We cannot add this new pdMap at this point due to sync issues.
        try {
            pdMap = XMLRootElementUtil.createPropertyDescriptorMap(cls);
        } catch (Throwable t) {
            throw ExceptionFactory.makeWebServiceException(t);
        }
        return pdMap;
    }

    void setPropertyDescriptorMapCache(Map<Class, Map<String, PropertyDescriptorPlus>> cache) {
        this.pdMapCache = cache;
    }

    public String getRequestWrapperClassName(OperationDescription operationDesc) {
        return requestWrapperMap.get(operationDesc);
    }

    void setRequestWrapperMap(Map<OperationDescription, String> map) {
        requestWrapperMap = map;
    }

    public String getResponseWrapperClassName(OperationDescription operationDesc) {
        return responseWrapperMap.get(operationDesc);
    }

    void setResponseWrapperMap(Map<OperationDescription, String> map) {
        responseWrapperMap = map;
    }

    public FaultBeanDesc getFaultBeanDesc(FaultDescription faultDesc) {
        return faultBeanDescMap.get(faultDesc);
    }

    void setFaultBeanDescMap(Map<FaultDescription, FaultBeanDesc> map) {
        faultBeanDescMap = map;
    }
    
    
    /**
     * Get the Method for the specified OperationDescription
     */
    public Method getMethod(OperationDescription operationDesc) {
        return methodMap.get(operationDesc);
    }
    
    /**
     * Set the Map containing the OperationDescription->Method mapping
     */
    void setMethodMap(Map<OperationDescription, Method> map) {
        methodMap = map;
    }

    public String toString() {
        try {
            final String newline = "\n";
            final String sameline = " ";
            StringBuffer string = new StringBuffer();

            string.append(newline);
            string.append("  MarshalServiceRuntime:" + getKey());
            string.append(newline);
            String pkgs = (getPackages() == null) ? "none" : getPackages().toString();
            string.append("    Packages = " + pkgs);

            for (Entry<String, AnnotationDesc> entry : this.annotationMap.entrySet()) {
                string.append(newline);
                string.append("    AnnotationDesc cached for:" + entry.getKey());
                string.append(entry.getValue().toString());
            }

            for (Entry<Class, Map<String, PropertyDescriptorPlus>> entry : this.pdMapCache.entrySet()) {
                string.append(newline);
                string.append("    PropertyDescriptorPlus Map cached for:" +
                        entry.getKey().getCanonicalName());
                for (PropertyDescriptorPlus pdp : entry.getValue().values()) {
                    string.append(newline);
                    string.append("      propertyName   =" + pdp.getPropertyName());
                    string.append(newline);
                    string.append("        xmlName      =" + pdp.getXmlName());
                    string.append(newline);
                    string.append("        propertyType =" + pdp.getPropertyType().getCanonicalName());
                    string.append(newline);
                }
            }

            string.append("    RequestWrappers");
            for (Entry<OperationDescription, String> entry : this.requestWrapperMap.entrySet()) {
                string.append(newline);
                string.append("    Operation:" + entry.getKey().getJavaMethodName() +
                        " RequestWrapper:" + entry.getValue());
            }

            string.append("    ResponseWrappers");
            for (Entry<OperationDescription, String> entry : this.responseWrapperMap.entrySet()) {
                string.append(newline);
                string.append("    Operation:" + entry.getKey().getJavaMethodName() +
                        " ResponseWrapper:" + entry.getValue());
            }

            string.append("    FaultBeanDesc");
            for (Entry<FaultDescription, FaultBeanDesc> entry : this.faultBeanDescMap.entrySet()) {
                string.append(newline);
                string.append("    FaultException:" + entry.getKey().getExceptionClassName());
                string.append(newline);
                string.append(entry.getValue().toString());
            }
            
            string.append("    Methods");
            for (Entry<OperationDescription, Method> entry : this.methodMap.entrySet()) {
                string.append(newline);
                string.append("    Method Name:" + entry.getKey().getJavaMethodName() +
                        " Method:" + entry.getValue().toString());
            }


            return string.toString();
        } catch (Throwable t) {
            // A problem occurred while dumping the contents.
            // This should not be re-thrown.
            return "An error occured while dumping the debug contents of MarshalServiceRuntimeDescriptionImpl:" + t.toString();
        }
    }


    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

}
