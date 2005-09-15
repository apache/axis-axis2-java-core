package org.apache.axis2.mtom.interop.util;
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
*
*
*/

import org.apache.axis2.om.*;
import org.apache.axis2.om.impl.llom.OMTextImpl;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;

/**
 * white Mesa Cr interop test
 */
public class BodyElements {

    public static OMElement bodySingle(boolean optimized) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNS = fac.createOMNamespace("http://example.org/mtom/data", "x");
        OMElement data = fac.createOMElement("Data", omNS);
        File file = null;
        DataHandler handler = null;
        file = new File("modules/integration/itest-resources/mtom/mtom.bin");
        if (file.exists()) {
            handler = new DataHandler(new FileDataSource(file));//
        } else {
            file = new File("itest-resources/mtom/mtom.bin");
            if (file.exists()) {
                handler = new DataHandler(new FileDataSource(file));//
            }
        }

        OMText txt = fac.createText(handler, optimized);
        data.addChild(txt);
        return data;
    }

    public static OMElement bodyMultiple(boolean optimzed, int repeat) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNS = fac.createOMNamespace("http://example.org/mtom/data", "x");
        OMElement echoTest = fac.createOMElement("EchoTest", omNS);
//
        File file = null;
        DataHandler handler = null;
        file = new File("modules/integration/itest-resources/mtom/mtom.bin");
        if (file.exists()) {
            handler = new DataHandler(new FileDataSource(file));//
        } else {
            file = new File("itest-resources/mtom/mtom.bin");
            if (file.exists()) {
                handler = new DataHandler(new FileDataSource(file));//
            }
        }
        for (int i = 0 ; i < repeat ; i++ ) {
            OMElement ele = fac.createOMElement("Data",omNS);
            OMText txt = fac.createText(handler,optimzed);
            ele.addChild(txt);
            echoTest.addChild(ele);
        }        
        return echoTest;
    }

    public static OMText getOriginalText(boolean optimized) {
        File file = new File("modules/integration/itest-resources/mtom/mtom.bin");
        DataHandler handler = new DataHandler(new FileDataSource(file));//
        OMText txt = new OMTextImpl(handler, optimized);
        return txt;
    }

}
