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
package org.apache.axis2.rpc;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * RPCMethod
 */
public class RPCMethod {
    static class Outerator implements Iterator {
        ArrayList params;
        RPCParameter cache = null;
        int badMode;
        int idx = 0;

        public Outerator(ArrayList params, int badMode) {
            this.params = params;
            this.badMode = badMode;
        }

        public void remove() {

        }

        public boolean hasNext() {
            if (cache != null) return true;
            while (idx < params.size()) {
                RPCParameter param = (RPCParameter)params.get(idx);
                idx++;
                if (param.mode != badMode) {
                    cache = param;
                    return true;
                }
            }
            return false;
        }

        public Object next() {
            RPCParameter ret = null;
            if (hasNext()) {
                ret = cache;
                cache = null;
            }
            return ret;
        }
    }

    int numInParams, numOutParams;
    Map parameters = new HashMap();
    ArrayList orderedParameters = new ArrayList();
    Method javaMethod;

    QName qname;
    QName responseQName;

    public RPCMethod(QName qname) {
        this.qname = qname;
    }

    public ArrayList getParams() {
        return orderedParameters;
    }

    public void addParameter(RPCParameter param) {
        orderedParameters.add(param);
        parameters.put(param.getQName(), param);
        if (param.getMode() != RPCParameter.MODE_OUT) {
            numInParams++;
        }
        if (param.getMode() != RPCParameter.MODE_IN) {
            numOutParams++;
        }
    }

    public RPCParameter getParameter(QName qname) {
        return (RPCParameter)parameters.get(qname);
    }

    public Method getJavaMethod() {
        return javaMethod;
    }

    public void setJavaMethod(Method javaMethod) {
        this.javaMethod = javaMethod;
    }

    public QName getResponseQName() {
        return responseQName;
    }

    public void setResponseQName(QName responseQName) {
        this.responseQName = responseQName;
    }

    public QName getQName() {
        return qname;
    }

    public RPCParameter getResponseParameter() {
        for (Iterator i = orderedParameters.iterator(); i.hasNext();) {
            RPCParameter parameter = (RPCParameter) i.next();
            if (parameter.getMode() == RPCParameter.MODE_OUT)
                return parameter;
        }
        return null;
    }

    public Iterator getOutParams() {
        return new Outerator(orderedParameters, RPCParameter.MODE_IN);
    }

    public Iterator getInParams() {
        return new Outerator(orderedParameters, RPCParameter.MODE_OUT);
    }

    public int getNumInParams() {
        return numInParams;
    }

    public int getNumOutParams() {
        return numOutParams;
    }
}
