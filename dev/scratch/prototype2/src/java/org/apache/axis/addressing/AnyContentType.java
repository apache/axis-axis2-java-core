package org.apache.axis.addressing;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * <p/>
 * wsa:ReferenceProperties and wsa:ReferenceParameter will be extended from this.
 * This will contain a collection of name value pairs.
 */
public class AnyContentType {

    private String anyContentTypeName;
    private HashMap valueHolder;

    public AnyContentType() {
        valueHolder = new HashMap(5);
    }

    public void addReferenceValue(QName name, String value){
        valueHolder.put(name, value);
    }

    public String getReferenceValue(QName name){
        return (String) valueHolder.get(name);
    }
}
