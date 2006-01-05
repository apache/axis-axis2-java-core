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

import org.apache.axis2.om.OMElement;

/**
 * Interface Parameter
 */
public interface Parameter {

    /**
     * Field TEXT_PARAMETER
     */
    public static int TEXT_PARAMETER = 0;

    /**
     * Field OM_PARAMETER
     */
    public static int OM_PARAMETER = 1;

    /**
     * Method getName.
     *
     * @return Returns int.
     */
    public String getName();

    /**
     * Gets the whole parameter element.
     *
     * @return Returns <code>OMElement<code>.
     */
    public OMElement getParameterElement();

    /**
     * Method getParameterType.
     *
     * @return Returns int.
     */
    public int getParameterType();

    /**
     * Method getValue.
     *
     * @return Returns Object.
     */
    public Object getValue();

    /**
     * Method isLocked.
     *
     * @return Returns boolean.
     */
    public boolean isLocked();

    /**
     * Method setLocked.
     *
     * @param value
     */
    public void setLocked(boolean value);

    /**
     * Method setName.
     *
     * @param name
     */
    public void setName(String name);

    /**
     * Parameter can be any thing - it can be XML element with number of child elements. So if some
     * one wants to access the XML element we need to store that. At the deployment time , to store
     * the XMLelement of the parameter, use this method to store whole
     * <parameter name="ServiceClass1" locked="false">org.apache.axis2.sample.echo.EchoImpl</parameter>
     * element.
     *
     * @param element <code>OMElement<code>
     */
    public void setParameterElement(OMElement element);

    public void setParameterType(int type);

    /**
     * Method setValue.
     *
     * @param value
     */
    public void setValue(Object value);
}
