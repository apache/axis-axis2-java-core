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

/**
 * Interface Parameter
 */
public interface Parameter {
    /**
     * Field TEXT_PARAMETER
     */
    public static int TEXT_PARAMETER = 0;

    /**
     * Field DOM_PARAMETER
     */
    public static int DOM_PARAMETER = 1;

    /**
     * Method getName
     *
     * @return
     */
    public String getName();

    /**
     * Method getValue
     *
     * @return
     */
    public Object getValue();

    /**
     * Method setName
     *
     * @param name
     */
    public void setName(String name);

    /**
     * Method setValue
     *
     * @param value
     */
    public void setValue(String value);

    /**
     * Method isLocked
     *
     * @return
     */
    public boolean isLocked();

    /**
     * Method setLocked
     *
     * @param value
     */
    public void setLocked(boolean value);

    /**
     * Method getParameterType
     *
     * @return
     */
    public int getParameterType();
}
