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

package org.apache.axis2.security.trust;

import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.impl.builder.StAXOMBuilder;
import org.apache.ws.security.util.Loader;

import javax.xml.namespace.QName;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Iterator;

public class TokenRequestDispatcherConfig {

    public final static String CONFIG_PARAM_KEY = "token-dispatcher-configuration";
    public final static String CONFIG_FILE_KEY = "token-dispatcher-configuration-file";
    
    private final static QName CONFIG = new QName("token-dispatcher-configuration");
    public final static QName ISSUER = new QName("issuer");
    public final static QName TOKEN_TYPE = new QName("tokenType");
    public final static QName CLASS_ATTR = new QName("class");
    public final static QName DEFAULT_ATTR = new QName("default");
    
    private Hashtable issuers;
    
    private String defaultIssuerClassName;
    
    
    public static TokenRequestDispatcherConfig load(OMElement configElem)
            throws TrustException {
        
        if(!CONFIG.equals(configElem.getQName())) {
            throw new TrustException("incorrectConfiguration");
        }
        
        TokenRequestDispatcherConfig conf = new TokenRequestDispatcherConfig();
        
        Iterator issuerElems = configElem.getChildrenWithName(ISSUER);
        while (issuerElems.hasNext()) {
            OMElement element = (OMElement) issuerElems.next();
            //get the class attr
            String classAttr = element.getAttributeValue(CLASS_ATTR);
            if(classAttr == null) {
                throw new TrustException("missingClassName");
            }
            String isDefault = element.getAttributeValue(DEFAULT_ATTR);
            if(isDefault != null && "true".equalsIgnoreCase(isDefault)) {
                //Use the first default issuer as the default isser
                if(conf.defaultIssuerClassName == null) {
                    conf.defaultIssuerClassName = classAttr;
                }
            }
            //Process token types
            Iterator tokenTypes = element.getChildrenWithName(TOKEN_TYPE);
            while (tokenTypes.hasNext()) {
                OMElement type = (OMElement) tokenTypes.next();
                String value = type.getText();
                if(value == null || "".equals(value)) {
                    throw new TrustException("invalidTokenTypeDefinition",
                            new String[] { "Issuer", classAttr });
                }
                if(conf.issuers == null) {
                    conf.issuers = new Hashtable();
                }
                //If the token type is not aleady declared then add it to the 
                //table with the issuer classname
                if(conf.issuers.keySet().size() > 0 && !conf.issuers.keySet().contains(value)) {
                    conf.issuers.put(value, classAttr);
                }
            }
        }
        
        //There must be a defulat issuer
        if(conf.defaultIssuerClassName == null) {
            throw new TrustException("defaultIssuerMissing");
        }
        
        return conf;
    }

    public static TokenRequestDispatcherConfig load(String configFilePath)
            throws TrustException {
        FileInputStream fis = null;
        StAXOMBuilder builder = null;
        try {
            fis = new FileInputStream(configFilePath);
             builder = new StAXOMBuilder(fis);
        } catch (Exception e) {
            throw new TrustException("errorLoadingConfigFile", new String[] {configFilePath});
        }
        
        return load(builder.getDocumentElement());
        
    }
    
    public TokenIssuer getDefaultIssuerInstace() throws TrustException {
        if(this.defaultIssuerClassName != null) {
            try {
                return (TokenIssuer) Loader.loadClass(
                        this.defaultIssuerClassName).newInstance();
            } catch (Exception e) {
                throw new TrustException("cannotLoadClass",
                        new String[] { this.defaultIssuerClassName }, e);
            }
        } else {
            return null;
        }
    }
    
    public String getDefaultIssuerName() {
        return this.defaultIssuerClassName;
    }
    
    
    public TokenIssuer getIssuer(String tokenType) throws TrustException {
        String issuerClassName = null;
        //try to find the isser class name from the tokenType<->issuer map
        if(this.issuers != null) {
            issuerClassName = (String)this.issuers.get(tokenType);
        }
        //If a specific issuer is not found use the default issuer
        if(issuerClassName == null) {
            issuerClassName = this.defaultIssuerClassName;
        }
        try {
            return (TokenIssuer) Loader.loadClass(
                    this.defaultIssuerClassName).newInstance();
        } catch (Exception e) {
            throw new TrustException("cannotLoadClass",
                    new String[] { this.defaultIssuerClassName }, e);
        }
        
    }
}
