/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.apache.axis2.clustering.context;

import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.PropertyDifference;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 */
public class PropertyUpdater implements Serializable {

    private Map properties;

    public void updateProperties(AbstractContext abstractContext) {
        System.err.println("----- updating props in " + abstractContext);
        for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            PropertyDifference propDiff =
                    (PropertyDifference) properties.get(key);
            if (propDiff.isRemoved()) {
                abstractContext.removePropertyNonReplicable(key);
            } else {  // it is updated/added
                abstractContext.setNonReplicableProperty(key, propDiff.getValue());
                System.err.println("........ added prop=" + key + ", value="+ propDiff.getValue() + " to context " + abstractContext);
            }
        }
    }

    public void addContextProperty(PropertyDifference diff) {
        properties.put(diff.getKey(), diff);
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public Map getProperties() {
        return properties;
    }
}
