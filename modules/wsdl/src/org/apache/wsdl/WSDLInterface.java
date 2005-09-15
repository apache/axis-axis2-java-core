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
package org.apache.wsdl;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;

/**
 * @author chathura@opensource.lk
 */
public interface WSDLInterface extends ExtensibleComponent {
    /**
     * Method getDefinedOperations
     *
     * @return
     */
    public HashMap getDefinedOperations();

    /**
     * Will return a map of all this <code>WSDLOperation</code>s that
     * are defined and inherited from super interfaces.
     *
     * @return
     */
    public HashMap getAllOperations();

    /**
     * @return
     */
    public List getFaults();

    /**
     * @return
     */
    public QName getName();

    /**
     * @return
     */
    public HashMap getOperations();

    /**
     * Retruns the <code>WSDLOperation</code>
     *
     * @param nCName
     * @return
     */
    public WSDLOperation getOperation(String nCName);

    /**
     * @return
     */
    public HashMap getSuperInterfaces();

    /**
     * Method getSuperInterface
     *
     * @param qName
     * @return
     */
    public WSDLInterface getSuperInterface(QName qName);

    /**
     * The Targetnamespace is that of the namespace URI of the QName of
     * this component.
     *
     * @return URI as a String if the name is set otherwise will return null.
     */
    public String getTargetnamespace();

    /**
     * @param list
     */
    public void setFaults(List list);

    /**
     * @param qName
     */
    public void setName(QName qName);

    /**
     * @param list
     */
    public void setOperations(HashMap list);

    /**
     * The operation is added by its ncname. If operation is null
     * it will not be added. If the Operation name is null a
     * <code>WSDLProcessingException</code> will be thrown.
     *
     * @param operation
     */
    public void setOperation(WSDLOperation operation);

    /**
     * @param list
     */
    public void setSuperInterfaces(HashMap list);

    /**
     * The Inteface will be added to the list of super interfaces keyed with
     * the QName.
     *
     * @param interfaceComponent WSDLInterface Object
     */
    public void addSuperInterface(WSDLInterface interfaceComponent);

    /**
     * Will return the StyleDefault if exist , otherwise will return null
     *
     * @return
     */
    public String getStyleDefault();

    /**
     * Method setStyleDefault
     *
     * @param styleDefault
     */
    public void setStyleDefault(String styleDefault);


}
