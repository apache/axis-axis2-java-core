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
package org.apache.axis2.jaxws.proxy.rpclit;

import java.math.BigInteger;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlList;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceProvider;

import org.apache.axis2.jaxws.proxy.rpclit.sei.RPCLit;
import org.test.proxy.rpclit.ComplexAll;
import org.test.proxy.rpclit.Enum;

/**
 * 
 *
 */
@WebService(targetNamespace="http://org/apache/axis2/jaxws/proxy/rpclit",
        endpointInterface="org.apache.axis2.jaxws.proxy.rpclit.sei.RPCLit")
public class RPCLitImpl implements RPCLit {

    
    
    /**
     * Echo the input
     */
    public String testSimple(String simpleIn) {
        return simpleIn;
    }
    
    public QName[] testLists(
            QName[] qNames,
            XMLGregorianCalendar[] calendars,
            String[] texts,
            BigInteger[] bigInts,
            Long[] longs,
            Enum[] enums,
            String[] text2,
            ComplexAll all) {
        // TODO Auto-generated method stub
        return null;
    }

    public XMLGregorianCalendar[] testCalendarList1(XMLGregorianCalendar[] cals) {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] testStringList2(String[] arg20) {

        assertTrue(arg20.length==2);
        assertTrue(arg20[0].equals("Hello"));
        assertTrue(arg20[1].equals("World"));
        return arg20;
    }

    public BigInteger[] testBigIntegerList3(BigInteger[] arg30) {
        // TODO Auto-generated method stub
        return null;
    }

    public Long[] testLongList4(Long[] longs) {
        assertTrue(longs.length==3);
        assertTrue(longs[0] == 0);
        assertTrue(longs[1] == 1);
        assertTrue(longs[2] == 2);
        return longs;
    }

    public Enum[] testEnumList5(Enum[] enums) {
        assertTrue(enums.length==3);
        assertTrue(enums[0] == Enum.ONE);
        assertTrue(enums[1] == Enum.TWO);
        assertTrue(enums[2] == Enum.THREE);
        return enums;
    }

    public ComplexAll testComplexAll6(ComplexAll arg60) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public String[] testEnumList7(String[] arg70) {
        // TODO Auto-generated method stub
        return null;
    }

    private void assertTrue(boolean value) throws RuntimeException {
        if (!value) {
            throw new RuntimeException();
        }
    }
}
