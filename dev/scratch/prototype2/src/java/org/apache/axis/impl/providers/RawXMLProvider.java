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

package org.apache.axis.impl.providers;

import org.apache.axis.context.MessageContext;
import org.apache.axis.context.SessionContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Constants;
import org.apache.axis.engine.Provider;
import org.apache.axis.engine.Service;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.registry.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This is a Simple java Provider. 
 * @author Srinath Perera(hemapani@opensource.lk)
 */

public class RawXMLProvider extends AbstractProvider implements Provider {
    protected Log log = LogFactory.getLog(getClass());
    
    private String message;
    private QName name;
    private String scope;
    private Method method;
    private ClassLoader classLoader;
    
    public RawXMLProvider(){
        scope = Constants.APPLICATION_SCOPE;

    }
    
    protected Object makeNewServiceObject(MessageContext msgContext)
        throws AxisFault
    {
        try {
            Service service = msgContext.getService();
            classLoader = service.getClassLoader();
            Parameter classParm = service.getParameter("className");
            String className = (String)classParm.getValue();
            if(className == null)
                throw new AxisFault("className parameter is null");
            if(classLoader == null){
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            Class implClass =Class.forName(className,true,classLoader);
            return implClass.newInstance();
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    public Object getTheImplementationObject(
            MessageContext msgContext)throws AxisFault{
            Service service = msgContext.getService();
            QName serviceName = service.getName();
        if(Constants.APPLICATION_SCOPE.equals(scope)){
            return makeNewServiceObject(msgContext);
        }else if(Constants.SESSION_SCOPE.equals(scope)){
            SessionContext sessionContext = msgContext.getSessionContext();
            Object obj = sessionContext.get(serviceName);
            if(obj == null){
                obj = makeNewServiceObject(msgContext);
                sessionContext.put(serviceName,obj);
            }
            return obj;            
        }else if(Constants.GLOBAL_SCOPE.equals(scope)){
            SessionContext globalContext = msgContext.getSessionContext();
            Object obj = globalContext.get(serviceName);
            if(obj == null){
                obj = makeNewServiceObject(msgContext);
                globalContext.put(serviceName,obj);
            }
            return obj;
        }else{
            throw new AxisFault("unknown scope "+ scope);
        }
            
    } 
    


    public QName getName() {
        return name;
    }

    public MessageContext invoke(MessageContext msgContext) throws AxisFault {
        try {
            //get the implementation class for the Web Service 
            Object obj = getTheImplementationObject(msgContext);
            
            //find the WebService method  
            Class ImplClass =obj.getClass();
            String methodName = msgContext.getOperation().getName().getLocalPart();
            Method[] methods = ImplClass.getMethods();
            for(int i = 0;i<methods.length;i++){
                if(methods[i].getName().equals(methodName)){
                    this.method = methods[i];
                    break;
                }
            }

            Object[] parms = new Object[]{msgContext.getEnvelope()};
            //invoke the WebService 
            SOAPEnvelope result = (SOAPEnvelope)method.invoke(obj,parms);
            MessageContext msgContext1 = new MessageContext(msgContext.getGlobalContext().getRegistry());
            msgContext1.setEnvelope(result);
            
            return msgContext1;
        }  catch (SecurityException e) {
            throw AxisFault.makeFault(e);
        } catch (IllegalArgumentException e) {
            throw AxisFault.makeFault(e);
        } catch (IllegalAccessException e) {
            throw AxisFault.makeFault(e);
        } catch (InvocationTargetException e) {
            throw AxisFault.makeFault(e);
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    public void revoke(MessageContext msgContext) {
        log.info("I am Speaking Provider revoking :)");
    }

    public void setName(QName name) {
        this.name = name;
    }

    /**
     * @return
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * @param loader
     */
    public void setClassLoader(ClassLoader loader) {
        classLoader = loader;
    }

}
