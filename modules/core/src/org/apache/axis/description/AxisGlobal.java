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
package org.apache.axis.description;

import org.apache.axis.engine.MessageReceiver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;

import javax.xml.namespace.QName;

/**
 * <p>This holds the information shown in the global scope. The information are all
 * that not goes in to the Transport or a Service. This has two types of Info. </p>
 * <ol>
 * <li>parameters<li>
 * <li>ordered phases<li>
 * <li>names of modules that are ref by the server.xml file, real modues are in the
 * Registry.<li>
 * <ol>
 * <p>Note: handlers in the server.xml file are not suported for M1, only way to put a
 * global handler is via a modules</p>
 */
public class AxisGlobal implements ParameterInclude {
    /**
     * Field paramInclude
     */
    protected final ParameterInclude paramInclude;

    /**
     * Field modules
     */
    protected final List modules;

    protected HashMap messagRecievers;

    // TODO provide a way to store name (name attribute value server.xml)

    /**
     * Constructor AxisGlobal
     */
    public AxisGlobal() {
        paramInclude = new ParameterIncludeImpl();
        modules = new ArrayList();
        messagRecievers = new HashMap();
    }
    

    public void addMessageReceiver(String key ,MessageReceiver messageReceiver){
        messagRecievers.put(key,messageReceiver) ;
    }

    public MessageReceiver getMessageReceiver(String key){
        return (MessageReceiver)messagRecievers.get(key);
    }

    /**
     * Method addModule
     *
     * @param moduleref
     */
    public void addModule(QName moduleref) {
        modules.add(moduleref);
    }

    /**
     * Method getModules
     *
     * @return
     */
    public Collection getModules() {
        return modules;
    }

    /**
     * Method getParameter
     *
     * @param name
     * @return
     */
    public Parameter getParameter(String name) {
        return paramInclude.getParameter(name);
    }

    /**
     * Method addParameter
     *
     * @param param
     */
    public void addParameter(Parameter param) {
        paramInclude.addParameter(param);
    }


}
