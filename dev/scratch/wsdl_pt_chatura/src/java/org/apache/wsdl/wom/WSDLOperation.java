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

import java.net.URI;
import java.util.List;


/**
 * @author chathura@opensource.lk
 *
 */
public interface WSDLOperation {
    public List getFeatures();

    public void setFeatures(List features);

    public List getProperties();

    public void setProperties(List properties);

    public List getInfaults();

    public void setInfaults(List infaults);

    public MessageReference getInputMessage();

    public void setInputMessage(MessageReference inputMessage);

    public int getMessageExchangePattern();

    public void setMessageExchangePattern(int messageExchangePattern);

    public String getName();

    public void setName(String name);

    public List getOutfaults();

    public void setOutfaults(List outfaults);

    public MessageReference getOutputMessage();

    public void setOutputMessage(MessageReference outputMessage);

    public boolean isSafe();

    public void setSafety(boolean safe);

    public int getStyle();

    public void setStyle(int style);

    public URI getTargetnemespace();

    public void setTargetnemespace(URI targetnemespace);
}