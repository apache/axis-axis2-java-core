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

import java.util.ArrayList;

/**
 * Interface ParameterInclude
 */
public interface ParameterInclude {

    // parameters

    /**
     * Method addParameter
     *
     * @param param
     */
    public void addParameter(Parameter param) throws AxisFault;

    /**
     * Method getParameter
     *
     * @param name
     * @return
     */
    public Parameter getParameter(String name);

    /**
     * To get all the parameters in a given description
     * @return
     */
    ArrayList getParameters();

    /**
     * to check whether the parameter is locked at any level 
     */

    boolean isParameterLocked(String paramterName);

}
