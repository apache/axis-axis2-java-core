package org.apache.axis2.databinding.utils;

import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.databinding.ADBNameValuePair;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;

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
 *
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

public class DummyADBBean implements ADBBean {

    public XMLStreamReader getPullParser() {
        ADBNameValuePair adbNameValuePair;
        ArrayList propertyList = new ArrayList();
        propertyList.add(new ADBNameValuePair("FirstRelease", "0.90"));
        propertyList.add(new ADBNameValuePair("SecondRelease", "0.91"));
        propertyList.add(new ADBNameValuePair("ThirdRelease", "0.92"));

        QName releasesQName = new QName("Releases");
        return ADBPullParser.createPullParser(propertyList, releasesQName);
    }
}
