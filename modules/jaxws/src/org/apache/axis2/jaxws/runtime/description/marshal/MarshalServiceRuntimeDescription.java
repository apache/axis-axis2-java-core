/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.jaxws.runtime.description.marshal;

import java.util.Map;
import java.util.TreeSet;

import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceRuntimeDescription;
import org.apache.axis2.jaxws.utility.PropertyDescriptorPlus;


/**
 * Used to cache marshal information
 */
public interface MarshalServiceRuntimeDescription extends ServiceRuntimeDescription {
   
    /**
     * @return Set of package names for this service
     */
    public TreeSet<String> getPackages();
    
    /**
     * Get the AnnotationDesc for this class.
     * If one is not cached, a new one is creatd
     * @param cls
     * @return AnnotationDesc
     */
    public AnnotationDesc getAnnotationDesc(Class cls);
    
    
    /**
     * Get the PropertyDescriptor map for the class.
     * The key of the map is a child xml local name.
     * The value is a PropertyDescriptor, that will be used to 
     * set/get values from a bean of the indicated class
     * @param cls
     * @return get the cached copy or create a new one
     */
    public Map<String, PropertyDescriptorPlus> getPropertyDescriptorMap(Class cls);
    
    /**
     * @param operationDesc
     * @return specified or defaulted wrapper class name.  Always returns null if the wrapper class does not exist.
     */
    public String getRequestWrapperClassName(OperationDescription operationDesc);
    
    /**
     * @param operationDesc
     * @return specified or defaulted wrapper class name.  Always returns null if the wrapper class does not exist.
     */
    public String getResponseWrapperClassName(OperationDescription operationDesc);
  
    /**
     * @param faultDesc
     * @return FaultBeanDescriptor that describes the fault bean
     */
    public FaultBeanDesc getFaultBeanDesc(FaultDescription faultDesc);

}
