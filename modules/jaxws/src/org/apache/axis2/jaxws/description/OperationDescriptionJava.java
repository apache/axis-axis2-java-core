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


package org.apache.axis2.jaxws.description;

import javax.jws.WebResult;
import javax.jws.WebParam.Mode;
import javax.jws.soap.SOAPBinding;

public interface OperationDescriptionJava {
    
    public WebResult getAnnoWebResult();
    public String getAnnoRequestWrapperClassName();
    public String getAnnoRequestWrapperLocalName();
    public String getAnnoRequestWrapperTargetNamespace();
    
    public String getAnnoResponseWrapperClassName();
    public String getAnnoResponseWrapperLocalName();
    public String getAnnoResponseWrapperTargetNamespace();
    
    public SOAPBinding getAnnoSoapBinding();
    public javax.jws.soap.SOAPBinding.ParameterStyle getAnnoSoapBindingParameterStyle();
    public javax.jws.soap.SOAPBinding.Style getAnnoSoapBindingStyle();
    public javax.jws.soap.SOAPBinding.Use getAnnoSoapBindingUse();
    
    public String getAnnoWebMethodAction();
    public boolean getAnnoWebMethodExclude();
    public String getAnnoWebMethodOperationName();

    public Mode[] getAnnoWebParamModes();
    public String[] getAnnoWebParamNames();
    public String getAnnoWebParamTargetNamespace(String name);
    public String[] getAnnoWebParamTargetNamespaces();
    public boolean isAnnoWebParamHeader(String name);
    
    public boolean isWebResultAnnotationSpecified();
    public boolean getAnnoWebResultHeader();
    public String getAnnoWebResultName();
    public String getAnnoWebResultPartName();
    public String getAnnoWebResultTargetNamespace();
    
    public boolean isAnnoOneWay();
    
}