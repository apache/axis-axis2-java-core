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
package org.apache.wsdl.wom;

import java.util.List;


/**
 * @author chathura@opensource.lk
 *
 */
public interface ExtensibleComponent extends Component{
    /**
     * Will add a <code>WSDLFeature</code> to the feature list.
     * If feature is null it will not be added. 
     * 
     * <code>ExtensibleComponent</code>
     * @param feature
     */
    public void addFeature(WSDLFeature feature);

    /**
     * Will return the <code>WSDLFeature</code>s. If there aren't
     * any features an empty list will be returned.
     *     
     * @return
     */
    public List getFeatures();

    /**
     * Wll add the property to the component properties. If the property is null it will
     * not be added.
     * @param wsdlProperty
     */
    public void addPorperty(WSDLProperty wsdlProperty);

    /**
     * Returns the Component Properties. If none exist an empty list will be returned.
     * @return
     */
    public List getProperties();
}