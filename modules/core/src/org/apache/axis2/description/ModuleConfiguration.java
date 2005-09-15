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

package org.apache.axis2.description;

import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;
import java.util.ArrayList;
/**
 * This is to store deployment time data , described by
 * <moduleConfig module="modulename">
 *    <paramter> ....</parameter>
 * </moduleConfig>
 *
 * for the initilal stage this just keep set of paramters , but when the time
 * bean will be store more
 */

public class ModuleConfiguration implements ParameterInclude{

    private QName moduleName;
    private ParameterInclude paramterinclude;

    //to keep the pointer to its parent , only to access paramters
    private ParameterInclude parent;

    public ModuleConfiguration(QName moduleName, ParameterInclude parent) {
        this.moduleName = moduleName;
        this.parent = parent;
        paramterinclude = new ParameterIncludeImpl();
    }

    public QName getModuleName() {
        return moduleName;
    }

    public void addParameter(Parameter param) throws AxisFault {
        if(isParamterLocked(param.getName())){
            throw new AxisFault("Parmter is locked can not overide: " + param.getName());
        } else{
            paramterinclude.addParameter(param);
        }
    }

    public Parameter getParameter(String name) {
        return paramterinclude.getParameter(name);
    }

    public ArrayList getParameters() {
        return paramterinclude.getParameters();
    }

    public boolean isParamterLocked(String paramterName) {
        // checking the locked value of parent
        boolean loscked =  false;
        if (parent !=null) {
            loscked = parent.isParamterLocked(paramterName);
        }
        if(loscked){
            return true;
        } else {
            Parameter parameter = getParameter(paramterName);
            return parameter != null && parameter.isLocked();
        }
    }

}
