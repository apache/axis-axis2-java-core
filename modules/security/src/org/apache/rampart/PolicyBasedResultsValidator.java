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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.rampart.util.RampartUtil;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.secpolicy.model.SupportingToken;
import org.apache.ws.secpolicy.model.Token;
import org.apache.ws.secpolicy.model.UsernameToken;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.util.WSSecurityUtil;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

public class PolicyBasedResultsValidator {
    
    private static Log log = LogFactory.getLog(PolicyBasedResultsValidator.class);
    
    public void validate(ValidatorData data, Vector results) 
    throws RampartException {
        
        RampartMessageData rmd = data.getRampartMessageData();
        
        RampartPolicyData rpd = rmd.getPolicyData();
        
        //Check presence of timestamp
        if(rpd.isIncludeTimestamp()) {
            WSSecurityEngineResult tsResult = 
                WSSecurityUtil.fetchActionResult(results, WSConstants.TS);
            if(tsResult == null) {
                throw new RampartException("timestampMissing");
            }
            
        }
        
        //sig/encr
        Vector encryptedParts = RampartUtil.getEncryptedParts(rmd);
        if(rpd.isSignatureProtection() && isSignatureRequired(rpd)) {
            encryptedParts.add(new WSEncryptionPart(WSConstants.SIG_LN, 
                    WSConstants.SIG_NS, "Element"));
        }
        
        Vector signatureParts = RampartUtil.getSignedParts(rmd);
        validateEncrSig(encryptedParts, signatureParts, results);
        
        validateProtectionOrder(data, results);
        
        validateEncryptedParts(data, results);

        //Supporting tokens
        if(!rmd.isClientSide()) {
            validateSupportingTokens(data, results);
        }
        
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
    
    /**
     * @param encryptedParts
     * @param signatureParts
     */
    private void validateEncrSig(Vector encryptedParts, Vector signatureParts, Vector results) 
    throws RampartException {
        ArrayList actions = getSigEncrActions(results);
        boolean sig = false; 
        boolean encr = false;
        for (Iterator iter = actions.iterator(); iter.hasNext();) {
            Integer act = (Integer) iter.next();
            if(act.intValue() == WSConstants.SIGN) {
                sig = true;
            } else if(act.intValue() == WSConstants.ENCR) {
                encr = true;
            }
        }
        
        if(sig && signatureParts.size() == 0) {
            
            //Unexpected signature
            throw new RampartException("unexprectedSignature");
        } else if(!sig && signatureParts.size() > 0) {
            
            //required signature missing
            throw new RampartException("signatureMissing");
        }
        
        if(encr && encryptedParts.size() == 0) {
            
            //Check whether its just an encrypted key
            ArrayList list = this.getResults(results, WSConstants.ENCR);
            boolean encrDataFound = false;
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                WSSecurityEngineResult result = (WSSecurityEngineResult) iter.next();
                if(result.getDataRefUris() != null) {
                    encrDataFound = true;
                }
            }
            if(encrDataFound) {
                //Unexpected encryption
                throw new RampartException("unexprectedEncryptedPart");
            }
        } else if(!encr && encryptedParts.size() > 0) {
            
            //required signature missing
            throw new RampartException("encryptionMissing");
        }
    }

    /**
     * @param data
     * @param results
     */
    private void validateSupportingTokens(ValidatorData data, Vector results) 
    throws RampartException {
        
        //Check for UsernameToken
        RampartPolicyData rpd = data.getRampartMessageData().getPolicyData();
        SupportingToken suppTok = rpd.getSupportingTokens();
        handleSupportingTokens(results, suppTok);
        SupportingToken signedSuppToken = rpd.getSignedSupportingTokens();
        handleSupportingTokens(results, signedSuppToken);
        SupportingToken signedEndSuppToken = rpd.getSignedEndorsingSupportingTokens();
        handleSupportingTokens(results, signedEndSuppToken);
        SupportingToken endSuppToken = rpd.getEndorsingSupportingTokens();
        handleSupportingTokens(results, endSuppToken);
    }

    /**
     * @param results
     * @param suppTok
     * @throws RampartException
     */
    private void handleSupportingTokens(Vector results, SupportingToken suppTok) throws RampartException {
        
        if(suppTok == null) {
            return;
        }
        
        ArrayList tokens = suppTok.getTokens();
        for (Iterator iter = tokens.iterator(); iter.hasNext();) {
            Token token = (Token) iter.next();
            if(token instanceof UsernameToken) {
                //Check presence of a UsernameToken
                WSSecurityEngineResult utResult = WSSecurityUtil.fetchActionResult(results, WSConstants.UT);
                if(utResult == null) {
                    throw new RampartException("usernameTokenMissing");
                }
                
            }
        }
    }
    
    
    

    /**
     * @param data
     * @param results
     */
    private void validateProtectionOrder(ValidatorData data, Vector results) 
    throws RampartException {
        
        String protectionOrder = data.getRampartMessageData().getPolicyData().getProtectionOrder();
        ArrayList sigEncrActions = this.getSigEncrActions(results);
        
        if(sigEncrActions.size() < 2) {
            //There are no results to COMPARE
            return;
        }
        boolean done = false;
        if(Constants.SIGN_BEFORE_ENCRYPTING.equals(protectionOrder)) {
            boolean sigfound = false;
            for (Iterator iter = sigEncrActions.iterator(); 
                iter.hasNext() || !done;) {
                Integer act = (Integer) iter.next();
                if(act.intValue() == WSConstants.SIGN) {
                    sigfound = true;
                } else if(sigfound) {
                    //We have an ENCR action after sig
                    done = true;
                }
            }
            
        } else {
            boolean encrFound = false;
            for (Iterator iter = sigEncrActions.iterator(); iter.hasNext();) {
                Integer act = (Integer) iter.next();
                if(act.intValue() == WSConstants.ENCR) {
                    encrFound = true;
                } else if(encrFound) {
                    //We have an ENCR action after sig
                    done = true;
                }
            }
        }
        
        if(!done) {
            throw new RampartException("protectionOrderMismatch");
        }
    }


    private ArrayList getSigEncrActions(Vector results) {
        ArrayList sigEncrActions = new ArrayList();
        for (Iterator iter = results.iterator(); iter.hasNext();) {
            int action = ((WSSecurityEngineResult) iter.next()).getAction();
            if(WSConstants.SIGN == action || WSConstants.ENCR == action) {
                sigEncrActions.add(new Integer(action));
            }
            
        }
        return sigEncrActions;
    }

    private void validateEncryptedParts(ValidatorData data, Vector results) 
    throws RampartException {
        
        RampartMessageData rmd = data.getRampartMessageData();
        
        ArrayList encrRefs = getEncryptedReferences(results);
        
        RampartPolicyData rpd = rmd.getPolicyData();
        
        //Check for encrypted body
        if(rpd.isEncryptBody()) {
            
            if(!encrRefs.contains(data.getBodyEncrDataId())){
                throw new RampartException("encryptedPartMissing", 
                        new String[]{data.getBodyEncrDataId()});
            }
        }
        
        int refCount = 0;
        
        refCount += rpd.getEncryptedParts().size();
        
        if(encrRefs.size() != refCount) {
            throw new RampartException("invalidNumberOfEncryptedParts", 
                    new String[]{Integer.toString(refCount)});
        }
        
    }

    private boolean isSignatureRequired(RampartPolicyData rpd) {
        return (rpd.isSymmetricBinding() && rpd.getSignatureToken() != null) ||
                (!rpd.isSymmetricBinding() && !rpd.isTransportBinding() && 
                        rpd.getInitiatorToken() != null);
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

    
    private ArrayList getEncryptedReferences(Vector results) {
        
        //there can be multiple ref lists
        ArrayList encrResults = getResults(results, WSConstants.ENCR);
        
        ArrayList refs = new ArrayList();
        
        for (Iterator iter = encrResults.iterator(); iter.hasNext();) {
            WSSecurityEngineResult engineResult = (WSSecurityEngineResult) iter.next();
            ArrayList dataRefUris = engineResult.getDataRefUris();
            
            //take only the ref list processing results
            if(dataRefUris != null) {
                for (Iterator iterator = dataRefUris.iterator(); iterator
                        .hasNext();) {
                    String uri = (String) iterator.next();
                    refs.add(uri);
                }
            }
        }
        
        return refs;
    }
    
    
    
    private ArrayList getResults(Vector results, int action) {
        
        ArrayList list = new ArrayList();
        
        for (int i = 0; i < results.size(); i++) {
            // Check the result of every action whether it matches the given
            // action
            if (((WSSecurityEngineResult) results.get(i)).getAction() == action) {
                list.add((WSSecurityEngineResult) results.get(i));
            }
        }
        
        return list;
    }
}
