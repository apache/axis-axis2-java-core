package org.apache.axis2.util;

import org.apache.axis2.databinding.utils.ADBPullParser;

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

public class BeanSeriliazerUtil {

    public static XMLStreamReader getPullParserForObject(Object pojo){
        // TODO : Please fix this with your magical reflection code
        ArrayList propertList = new ArrayList();
        QName elementQName = null;

        return ADBPullParser.createPullParser(propertList.toArray(), elementQName);
    }

}
