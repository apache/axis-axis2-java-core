/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.wsdl.impl;

import org.apache.wsdl.ExtensibleComponent;
import org.apache.wsdl.WSDLFeature;
import org.apache.wsdl.WSDLProperty;

import java.util.LinkedList;
import java.util.List;

/**
 * @author chathura@opensource.lk
 *
 */
public class ExtensibleComponentImpl extends ComponentImpl implements ExtensibleComponent {

    
    private List features = null;
    
    private List properties = null;
    
    /**
     * Will add a <code>WSDLFeature</code> to the feature list.
     * If feature is null it will not be added. 
     * 
     * <code>ExtensibleComponent</code>
     * @param feature
     */
    public void addFeature(WSDLFeature feature){
        if(null == this.features) this.features = new LinkedList();
        
        if(null == feature) return ;
        
        this.features.add(feature);
    }
    /**
     * Will return the <code>WSDLFeature</code>s. If there aren't
     * any features an empty list will be returned.
     *     
     * @return
     */
    public List getFeatures(){
        if(null == this.features) return new LinkedList();
        return this.features;
    }
    
    /**
     * Wll add the property to the component properties. If the property is null it will
     * not be added.
     * @param wsdlProperty
     */
    public void addPorperty(WSDLProperty wsdlProperty){
        if(null == this.properties) this.properties = new LinkedList();
        
        if(null == wsdlProperty) return;
        
        this.features.add(wsdlProperty);
                
    }
    
    /**
     * Returns the Component Properties. If none exist an empty list will be returned.
     * @return
     */
    public List getProperties(){
        
        if(null == this.properties) return new LinkedList();
        
        return this.properties;
    }
    
}
