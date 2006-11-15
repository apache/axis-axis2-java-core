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

package org.apache.rahas;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.ws.security.util.Loader;

import javax.xml.namespace.QName;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class TokenRequestDispatcherConfig {

    public final static String CONFIG_PARAM_KEY = "token-dispatcher-configuration";
    public final static String CONFIG_FILE_KEY = "token-dispatcher-configuration-file";

    private final static QName DISPATCHER_CONFIG = new QName("token-dispatcher-configuration");
    private final static QName ISSUER = new QName("issuer");
    private final static QName CANCELER = new QName("canceler");
    private final static QName TOKEN_TYPE = new QName("tokenType");
    private final static QName CLASS_ATTR = new QName("class");
    private final static QName DEFAULT_ATTR = new QName("default");
    private final static QName CONFIGURATION_ELEMENT = new QName("configuration");

    private Map issuers;
    private Map configFiles = new Hashtable();
    private Map configElements = new Hashtable();
    private Map configParamNames = new Hashtable();

    private String defaultIssuerClassName;
    private String defaultCancelerClassName;

    public static TokenRequestDispatcherConfig load(OMElement configElem) throws TrustException {

        if (!DISPATCHER_CONFIG.equals(configElem.getQName())) {
            throw new TrustException("incorrectConfiguration");
        }
        TokenRequestDispatcherConfig conf = new TokenRequestDispatcherConfig();

        // Issuers
        handleIssuers(configElem, conf);

        // Cancelers
        handleCancelers(configElem, conf);

        //There must be a defulat issuer
        if (conf.defaultIssuerClassName == null) {
            throw new TrustException("defaultIssuerMissing");
        }
        return conf;
    }

    private static void handleCancelers(OMElement configElem,
                                        TokenRequestDispatcherConfig conf) throws TrustException {

        OMElement cancelerEle = configElem.getFirstChildWithName(CANCELER); // support only one canceler
        if (cancelerEle != null) {
            //get the class attr
            String cancelerClass = cancelerEle.getAttributeValue(CLASS_ATTR);
            if (cancelerClass == null) {
                throw new TrustException("missingClassName");
            }
            conf.defaultCancelerClassName = cancelerClass;
            processConfiguration(cancelerEle, conf, cancelerClass);
        }

        //TODO: imple
    }

    private static void handleIssuers(OMElement configElem,
                                      TokenRequestDispatcherConfig conf) throws TrustException {
        for (Iterator issuerElems = configElem.getChildrenWithName(ISSUER);
             issuerElems.hasNext();) {

            OMElement element = (OMElement) issuerElems.next();

            //get the class attr
            String issuerClass = element.getAttributeValue(CLASS_ATTR);
            if (issuerClass == null) {
                throw new TrustException("missingClassName");
            }
            String isDefault = element.getAttributeValue(DEFAULT_ATTR);
            if (isDefault != null && "true".equalsIgnoreCase(isDefault)) {
                //Use the first default issuer as the default isser
                if (conf.defaultIssuerClassName == null) {
                    conf.defaultIssuerClassName = issuerClass;
                } else {
                    throw new TrustException("badDispatcherConfigMultipleDefaultIssuers");
                }
            }

            processConfiguration(element, conf, issuerClass);

            //Process token types
            for (Iterator tokenTypes = element.getChildrenWithName(TOKEN_TYPE);
                 tokenTypes.hasNext();) {
                OMElement type = (OMElement) tokenTypes.next();
                String value = type.getText();
                if (value == null || value.trim().length() == 0) {
                    throw new TrustException("invalidTokenTypeDefinition",
                                             new String[]{"Issuer", issuerClass});
                }
                if (conf.issuers == null) {
                    conf.issuers = new Hashtable();
                }
                //If the token type is not aleady declared then add it to the
                //table with the issuer classname
                if (!conf.issuers.keySet().contains(value)) {
                    conf.issuers.put(value, issuerClass);
                }
            }
        }
    }

    private static void processConfiguration(OMElement element,
                                             TokenRequestDispatcherConfig conf,
                                             String implClass) {

        for (Iterator configs = element.getChildrenWithName(CONFIGURATION_ELEMENT);
             configs.hasNext();) {
            OMElement configEle = (OMElement) configs.next();
            String configType =
                    configEle.getAttribute(new QName("type")).getAttributeValue().trim();
            if (configType.equalsIgnoreCase("file")) { //Process configuration file information
                String issuerConfigFile = configEle.getText();
                if (issuerConfigFile != null) {
                    conf.configFiles.put(implClass, issuerConfigFile);
                }
            } else if (configType.equalsIgnoreCase("element"))
            { //Process configuration element information
                conf.configElements.put(implClass, configEle);
            } else if (configType.equalsIgnoreCase("parameter"))
            { //Process configuration parameter name information
                conf.configParamNames.put(implClass, configEle.getText());
            }
        }
    }

    public static TokenRequestDispatcherConfig load(String configFilePath) throws TrustException {
        FileInputStream fis;
        StAXOMBuilder builder;
        try {
            fis = new FileInputStream(configFilePath);
            builder = new StAXOMBuilder(fis);
        } catch (Exception e) {
            throw new TrustException("errorLoadingConfigFile", new String[]{configFilePath});
        }
        return load(builder.getDocumentElement());
    }

    public TokenIssuer getDefaultIssuerInstace() throws TrustException {
        if (this.defaultIssuerClassName != null) {
            try {
                return createIssuer(this.defaultIssuerClassName);
            } catch (Exception e) {
                throw new TrustException("cannotLoadClass",
                                         new String[]{this.defaultIssuerClassName}, e);
            }
        } else {
            return null;
        }
    }

    public TokenCanceler getDefaultCancelerInstance() throws TrustException {
        if (this.defaultCancelerClassName != null) {
            try {
                return createCanceler(this.defaultCancelerClassName);
            } catch (Exception e) {
                throw new TrustException("cannotLoadClass",
                                         new String[]{this.defaultCancelerClassName}, e);
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
        //try to find the issuer class name from the tokenType<->issuer map
        if (this.issuers != null) {
            issuerClassName = (String) this.issuers.get(tokenType);
        }
        //If a specific issuer is not found use the default issuer
        if (issuerClassName == null) {
            issuerClassName = this.defaultIssuerClassName;
        }
        try {
            return createIssuer(issuerClassName);
        } catch (Exception e) {
            throw new TrustException("cannotLoadClass",
                                     new String[]{this.defaultIssuerClassName}, e);
        }
    }

    /**
     * @param issuerClassName
     * @return TokenIssuer
     */
    private TokenIssuer createIssuer(String issuerClassName) throws Exception {
        TokenIssuer issuer = (TokenIssuer) Loader.loadClass(issuerClassName).newInstance();
        issuer.setConfigurationElement((OMElement) this.configElements.get(issuerClassName));
        issuer.setConfigurationFile((String) this.configFiles.get(issuerClassName));
        issuer.setConfigurationParamName((String) this.configParamNames.get(issuerClassName));
        return issuer;
    }

    private TokenCanceler createCanceler(String cancelerClassName) throws Exception {
        TokenCanceler canceler = (TokenCanceler) Loader.loadClass(cancelerClassName).newInstance();
        canceler.setConfigurationElement((OMElement) this.configElements.get(cancelerClassName));
        canceler.setConfigurationFile((String) this.configFiles.get(cancelerClassName));
        canceler.setConfigurationParamName((String) this.configParamNames.get(cancelerClassName));
        return canceler;
    }
}
