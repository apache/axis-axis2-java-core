package org.apache.axis.axiom;

import org.apache.xml.utils.QName;
import org.apache.axis.axiom.infoset.OmAttribute;

/**
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
 *
 * Date: 15-Sep-2004
 * Time: 14:32:27
 */
public class OMAttributeImpl implements OmAttribute {
    private QName qname;
    private String value;
    //do we need a qname here as the value???????????
    //how weird! But it seems that there are cases

    public QName getQname() {
        return qname;
    }

    public void setQname(QName qname) {
        this.qname = qname;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}