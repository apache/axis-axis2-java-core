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

package org.apache.rampart;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.rampart.util.Axis2Util;
import org.apache.rampart.util.RampartUtil;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.util.WSSecurityUtil;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class RampartEngine {

    private static Log log = LogFactory.getLog(RampartEngine.class);    

    public Vector process(MessageContext msgCtx) throws WSSPolicyException,
    RampartException, WSSecurityException, AxisFault {
        
        RampartMessageData rmd = new RampartMessageData(msgCtx, false);
        RampartPolicyData rpd = rmd.getPolicyData();
        if(rpd == null) {
            SOAPEnvelope env = Axis2Util.getSOAPEnvelopeFromDOOMDocument(rmd.getDocument());

            //Convert back to llom since the inflow cannot use llom
            msgCtx.setEnvelope(env);
            Axis2Util.useDOOM(false);
            return null;
        }
        Vector results = null;
        
        WSSecurityEngine engine = new WSSecurityEngine();
        
        if(rpd.isSymmetricBinding()) {
            //Here we have to create the CB handler to get the tokens from the 
            //token storage
            
            results = engine.processSecurityHeader(rmd.getDocument(), 
                                null, 
                                new TokenCallbackHandler(rmd.getTokenStorage(), RampartUtil.getPasswordCB(rmd)),
                                RampartUtil.getSignatureCrypto(rpd.getRampartConfig(), 
                                        msgCtx.getAxisService().getClassLoader()));
        } else {
            results = engine.processSecurityHeader(rmd.getDocument(),
                      null, 
                      RampartUtil.getPasswordCB(rmd),
                      RampartUtil.getSignatureCrypto(rpd.getRampartConfig(), 
                              msgCtx.getAxisService().getClassLoader()), 
                      RampartUtil.getEncryptionCrypto(rpd.getRampartConfig(), 
                              msgCtx.getAxisService().getClassLoader()));
        }
        

        SOAPEnvelope env = Axis2Util.getSOAPEnvelopeFromDOOMDocument(rmd.getDocument());

        //Convert back to llom since the inflow cannot use DOOM
        msgCtx.setEnvelope(env);
        Axis2Util.useDOOM(false);

        this.validateResults(rmd, results);
        
        return results;
    }


    private void validateResults(RampartMessageData rmd, Vector results) throws RampartException {

        /*
         * Now we can check the certificate used to sign the message. In the
         * following implementation the certificate is only trusted if either it
         * itself or the certificate of the issuer is installed in the keystore.
         * 
         * Note: the method verifyTrust(X509Certificate) allows custom
         * implementations with other validation algorithms for subclasses.
         */

        // Extract the signature action result from the action vector
        WSSecurityEngineResult actionResult = WSSecurityUtil.fetchActionResult(
                results, WSConstants.SIGN);

        if (actionResult != null) {
            X509Certificate returnCert = actionResult.getCertificate();

            if (returnCert != null) {
                if (!verifyTrust(returnCert, rmd)) {
                    throw new RampartException ("trustVerificationError");
                }
            }
        }

        /*
         * Perform further checks on the timestamp that was transmitted in the
         * header. In the following implementation the timestamp is valid if it
         * was created after (now-ttl), where ttl is set on server side, not by
         * the client.
         * 
         * Note: the method verifyTimestamp(Timestamp) allows custom
         * implementations with other validation algorithms for subclasses.
         */

        // Extract the timestamp action result from the action vector
        actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.TS);

        if (actionResult != null) {
            Timestamp timestamp = actionResult.getTimestamp();

            if (timestamp != null) {
                if (!verifyTimestamp(timestamp, rmd.getTimeToLive())) {
                    throw new RampartException("cannotValidateTimestamp");
                }
            }
        }

    }

    
    private boolean verifyTimestamp(Timestamp timestamp, int timeToLive) throws RampartException {

        // Calculate the time that is allowed for the message to travel
        Calendar validCreation = Calendar.getInstance();
        long currentTime = validCreation.getTime().getTime();
        currentTime -= timeToLive * 1000;
        validCreation.setTime(new Date(currentTime));

        // Validate the time it took the message to travel
        // if (timestamp.getCreated().before(validCreation) ||
        // !timestamp.getCreated().equals(validCreation)) {
        Calendar cre = timestamp.getCreated();
        if (cre != null && !cre.after(validCreation)) {
            return false;
        }

        return true;
    }
    
    /**
     * Evaluate whether a given certificate should be trusted.
     * Hook to allow subclasses to implement custom validation methods however they see fit.
     * <p/>
     * Policy used in this implementation:
     * 1. Search the keystore for the transmitted certificate
     * 2. Search the keystore for a connection to the transmitted certificate
     * (that is, search for certificate(s) of the issuer of the transmitted certificate
     * 3. Verify the trust path for those certificates found because the search for the issuer might be fooled by a phony DN (String!)
     *
     * @param cert the certificate that should be validated against the keystore
     * @return true if the certificate is trusted, false if not (AxisFault is thrown for exceptions during CertPathValidation)
     * @throws WSSecurityException
     */
    protected boolean verifyTrust(X509Certificate cert, RampartMessageData rmd) throws RampartException {

        // If no certificate was transmitted, do not trust the signature
        if (cert == null) {
            return false;
        }

        String[] aliases = null;
        String alias = null;
        X509Certificate[] certs;

        String subjectString = cert.getSubjectDN().getName();
        String issuerString = cert.getIssuerDN().getName();
        BigInteger issuerSerial = cert.getSerialNumber();
        
        boolean doDebug = log.isDebugEnabled();

        if (doDebug) {
            log.debug("WSHandler: Transmitted certificate has subject " + 
                    subjectString);
            log.debug("WSHandler: Transmitted certificate has issuer " + 
                    issuerString + " (serial " + issuerSerial + ")");
        }

        // FIRST step
        // Search the keystore for the transmitted certificate

        // Search the keystore for the alias of the transmitted certificate
        try {
            alias = RampartUtil.getSignatureCrypto(
                    rmd.getPolicyData().getRampartConfig(),
                    rmd.getCustomClassLoader()).getAliasForX509Cert(
                    issuerString, issuerSerial);
        } catch (WSSecurityException ex) {
            throw new RampartException("cannotFindAliasForCert", new String[]{subjectString}, ex);
        }

        if (alias != null) {
            // Retrieve the certificate for the alias from the keystore
            try {
                certs = RampartUtil.getSignatureCrypto(
                        rmd.getPolicyData().getRampartConfig(),
                        rmd.getCustomClassLoader()).getCertificates(alias);
            } catch (WSSecurityException ex) {
                throw new RampartException("noCertForAlias", new String[] {alias}, ex);
            }

            // If certificates have been found, the certificates must be compared
            // to ensure againgst phony DNs (compare encoded form including signature)
            if (certs != null && certs.length > 0 && cert.equals(certs[0])) {
                if (doDebug) {
                    log.debug("Direct trust for certificate with " + subjectString);
                }
                return true;
            }
        } else {
            if (doDebug) {
                log.debug("No alias found for subject from issuer with " + issuerString + " (serial " + issuerSerial + ")");
            }
        }

        // SECOND step
        // Search for the issuer of the transmitted certificate in the keystore

        // Search the keystore for the alias of the transmitted certificates issuer
        try {
            aliases = RampartUtil.getSignatureCrypto(
                    rmd.getPolicyData().getRampartConfig(),
                    rmd.getCustomClassLoader()).getAliasesForDN(issuerString);
        } catch (WSSecurityException ex) {
            throw new RampartException("cannotFindAliasForCert", new String[]{issuerString}, ex);
        }

        // If the alias has not been found, the issuer is not in the keystore
        // As a direct result, do not trust the transmitted certificate
        if (aliases == null || aliases.length < 1) {
            if (doDebug) {
                log.debug("No aliases found in keystore for issuer " + issuerString + " of certificate for " + subjectString);
            }
            return false;
        }

        // THIRD step
        // Check the certificate trust path for every alias of the issuer found in the keystore
        for (int i = 0; i < aliases.length; i++) {
            alias = aliases[i];

            if (doDebug) {
                log.debug("Preparing to validate certificate path with alias " + alias + " for issuer " + issuerString);
            }

            // Retrieve the certificate(s) for the alias from the keystore
            try {
                certs = RampartUtil.getSignatureCrypto(
                        rmd.getPolicyData().getRampartConfig(),
                        rmd.getCustomClassLoader()).getCertificates(alias);
            } catch (WSSecurityException ex) {
                throw new RampartException("noCertForAlias", new String[] {alias}, ex);
            }

            // If no certificates have been found, there has to be an error:
            // The keystore can find an alias but no certificate(s)
            if (certs == null | certs.length < 1) {
                throw new RampartException("noCertForAlias", new String[] {alias});
            }

            // Form a certificate chain from the transmitted certificate
            // and the certificate(s) of the issuer from the keystore
            // First, create new array
            X509Certificate[] x509certs = new X509Certificate[certs.length + 1];
            // Then add the first certificate ...
            x509certs[0] = cert;
            // ... and the other certificates
            for (int j = 0; j < certs.length; j++) {
                cert = certs[i];
                x509certs[certs.length + j] = cert;
            }
            certs = x509certs;

            // Use the validation method from the crypto to check whether the subjects certificate was really signed by the issuer stated in the certificate
            try {
                if (RampartUtil.getSignatureCrypto(
                        rmd.getPolicyData().getRampartConfig(),
                        rmd.getCustomClassLoader()).validateCertPath(certs)) {
                    if (doDebug) {
                        log.debug("WSHandler: Certificate path has been verified for certificate with subject " + subjectString);
                    }
                    return true;
                }
            } catch (WSSecurityException ex) {
                throw new RampartException("certPathVerificationFailed", new String[]{subjectString}, ex);
            }
        }

        log.debug("WSHandler: Certificate path could not be verified for certificate with subject " + subjectString);
        return false;
    }

}
