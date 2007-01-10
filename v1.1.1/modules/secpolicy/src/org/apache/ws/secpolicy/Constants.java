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

package org.apache.ws.secpolicy;

import javax.xml.namespace.QName;

public class Constants {

    public static final String P_NS = "http://schemas.xmlsoap.org/ws/2004/09/policy";

    public static final String P_PREFIX = "wsp";

    public static final QName POLICY = new QName(P_NS, "Policy", P_PREFIX);

    public final static String SP_NS = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";

    public final static String SP_PREFIX = "sp";

    public final static String ATTR_INCLUDE_TOKEN = "IncludeToken";

    public final static String INCLUDE_NEVER = Constants.SP_NS
            + "/IncludeToken/Never";

    public final static String INCLUDE_ONCE = Constants.SP_NS
            + "/IncludeToken/Once";

    public final static String INCLUDE_ALWAYS_TO_RECIPIENT = Constants.SP_NS
            + "/IncludeToken/AlwaysToRecipient";

    public final static String INCLUDE_ALWAYS = Constants.SP_NS
            + "/IncludeToken/Always";

    public final static int SUPPORTING_TOKEN_SUPPORTING = 1;

    public final static int SUPPORTING_TOKEN_ENDORSING = 2;

    public final static int SUPPORTING_TOKEN_SIGNED = 3;

    public final static int SUPPORTING_TOKEN_SIGNED_ENDORSING = 4;

    /**
     * Security Header Layout : Strict
     */
    public final static String LAYOUT_STRICT = "Strict";

    /**
     * Security Header Layout : Lax
     */
    public final static String LAYOUT_LAX = "Lax";

    /**
     * Security Header Layout : LaxTimestampFirst
     */
    public final static String LAYOUT_LAX_TIMESTAMP_FIRST = "LaxTimestampFirst";

    /**
     * Security Header Layout : LaxTimestampLast
     */
    public final static String LAYOUT_LAX_TIMESTAMP_LAST = "LaxTimestampLast";

    /**
     * Protection Order : EncryptBeforeSigning
     */
    public final static String ENCRYPT_BEFORE_SIGNING = "EncryptBeforeSigning";

    /**
     * Protection Order : SignBeforeEncrypting
     */
    public final static String SIGN_BEFORE_ENCRYPTING = "SignBeforeEncrypting";

    public final static String ONLY_SIGN_ENTIRE_HEADERS_AND_BODY = "OnlySignEntireHeadersAndBody";

    public final static String WSS_X509_V1_TOKEN10 = "WssX509V1Token10";

    public final static String WSS_X509_V3_TOKEN10 = "WssX509V3Token10";

    public final static String WSS_X509_PKCS7_TOKEN10 = "WssX509Pkcs7Token10";

    public final static String WSS_X509_PKI_PATH_V1_TOKEN10 = "WssX509PkiPathV1Token10";

    public final static String WSS_X509_V1_TOKEN11 = "WssX509V1Token11";

    public final static String WSS_X509_V3_TOKEN11 = "WssX509V3Token11";

    public final static String WSS_X509_PKCS7_TOKEN11 = "WssX509Pkcs7Token11";

    public final static String WSS_X509_PKI_PATH_V1_TOKEN11 = "WssX509PkiPathV1Token11";

    // /
    // /Algorithm Suites
    // /
    public final static String ALGO_SUITE_BASIC256 = "Basic256";

    public final static String ALGO_SUITE_BASIC192 = "Basic192";

    public final static String ALGO_SUITE_BASIC128 = "Basic128";

    public final static String ALGO_SUITE_TRIPLE_DES = "TripleDes";

    public final static String ALGO_SUITE_BASIC256_RSA15 = "Basic256Rsa15";

    public final static String ALGO_SUITE_BASIC192_RSA15 = "Basic192Rsa15";

    public final static String ALGO_SUITE_BASIC128_RSA15 = "Basic128Rsa15";

    public final static String ALGO_SUITE_TRIPLE_DES_RSA15 = "TripleDesRsa15";

    public final static String ALGO_SUITE_BASIC256_SHA256 = "Basic256Sha256";

    public final static String ALGO_SUITE_BASIC192_SHA256 = "Basic192Sha256";

    public final static String ALGO_SUITE_BASIC128_SHA256 = "Basic128Sha256";

    public final static String ALGO_SUITE_TRIPLE_DES_SHA256 = "TripleDesSha256";

    public final static String ALGO_SUITE_BASIC256_SHA256_RSA15 = "Basic256Sha256Rsa15";

    public final static String ALGO_SUITE_BASIC192_SHA256_RSA15 = "Basic192Sha256Rsa15";

    public final static String ALGO_SUITE_BASIC128_SHA256_RSA15 = "Basic128Sha256Rsa15";

    public final static String ALGO_SUITE_TRIPLE_DES_SHA256_RSA15 = "TripleDesSha256Rsa15";

    // /
    // /Algorithms
    // /
    public final static String HMAC_SHA1 = "http://www.w3.org/2000/09/xmldsig#hmac-sha1";

    public final static String RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";

    public final static String SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";

    public final static String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";

    public final static String SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";

    public final static String AES128 = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";

    public final static String AES192 = "http://www.w3.org/2001/04/xmlenc#aes192-cbc";

    public final static String AES256 = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";

    public final static String TRIPLE_DES = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";

    public final static String KW_AES128 = "http://www.w3.org/2001/04/xmlenc#kw-aes128";

    public final static String KW_AES192 = "http://www.w3.org/2001/04/xmlenc#kw-aes192";

    public final static String KW_AES256 = "http://www.w3.org/2001/04/xmlenc#kw-aes256";

    public final static String KW_TRIPLE_DES = "http://www.w3.org/2001/04/xmlenc#kw-tripledes";

    public final static String KW_RSA_OAEP = "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p";

    public final static String KW_RSA15 = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";

    public final static String P_SHA1 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";

    public final static String P_SHA1_L128 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";

    public final static String P_SHA1_L192 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";

    public final static String P_SHA1_L256 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";

    public final static String XPATH = "http://www.w3.org/TR/1999/REC-xpath-19991116";

    public final static String XPATH20 = "http://www.w3.org/2002/06/xmldsig-filter2";

    public final static String C14N = "http://www.w3.org/2001/10/xml-c14n#";

    public final static String EX_C14N = "http://www.w3.org/2001/10/xml-exc-c14n#";

    public final static String SNT = "http://www.w3.org/TR/soap12-n11n";

    public final static String STRT10 = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#STR-Transform";

    // //////////////////////////////////////////////////////////////////////

    public static final String INCLUSIVE_C14N = "InclusiveC14N";

    public static final String SOAP_NORMALIZATION_10 = "SoapNormalization10";

    public static final String STR_TRANSFORM_10 = "STRTransform10";

    public static final String XPATH10 = "XPath10";

    public static final String XPATH_FILTER20 = "XPathFilter20";

    // /////////////////////////////////////////////////////////////////////

    public static final QName ATTR_XPATH_VERSION = new QName(SP_NS, "XPathVersion", Constants.SP_PREFIX);
    
    ////////////////////////////////////////////////////////////////////////
    public static final QName INCLUDE_TOKEN = new QName(SP_NS, "IncludeToken",
            Constants.SP_PREFIX);

    public static final QName TRANSPORT_BINDING = new QName(SP_NS,
            "TransportBinding", Constants.SP_PREFIX);

    public static final QName ALGORITHM_SUITE = new QName(SP_NS,
            "AlgorithmSuite", Constants.SP_PREFIX);

    public static final QName LAYOUT = new QName(SP_NS, "Layout", SP_PREFIX);

    // ///////////////////

    public static final QName STRICT = new QName(Constants.SP_NS, "Strict",
            Constants.SP_PREFIX);

    public static final QName LAX = new QName(Constants.SP_NS, "Lax",
            Constants.SP_PREFIX);

    public static final QName LAXTSFIRST = new QName(Constants.SP_NS,
            "LaxTsFirst", Constants.SP_PREFIX);

    public static final QName LAXTSLAST = new QName(Constants.SP_NS,
            "LaxTsLast", Constants.SP_PREFIX);

    // ////////////////

    public static final QName INCLUDE_TIMESTAMP = new QName(SP_NS,
            "IncludeTimestamp", Constants.SP_PREFIX);

    public static final QName TRANSPORT_TOKEN = new QName(SP_NS,
            "TransportToken", Constants.SP_PREFIX);

    public static final QName HTTPS_TOKEN = new QName(Constants.SP_NS,
            "HttpsToken", Constants.SP_PREFIX);

    public static final QName SECURITY_CONTEXT_TOKEN = new QName(
            Constants.SP_NS, "SecurityContextToken", Constants.SP_PREFIX);

    public static final QName SECURE_CONVERSATION_TOKEN = new QName(
            Constants.SP_NS, "SecureConversationToken", Constants.SP_PREFIX);

    public static final QName SIGNATURE_TOKEN = new QName(Constants.SP_NS,
            "SignatureToken", Constants.SP_PREFIX);

    public static final QName SIGNED_PARTS = new QName(Constants.SP_NS,
            "SignedParts", Constants.SP_PREFIX);

    public static final QName USERNAME_TOKEN = new QName(Constants.SP_NS,
            "UsernameToken", Constants.SP_PREFIX);

    public static final QName WSS_USERNAME_TOKEN10 = new QName(Constants.SP_NS,
            "WssUsernameToken10", Constants.SP_PREFIX);

    public static final QName WSS_USERNAME_TOKEN11 = new QName(Constants.SP_NS,
            "WssUsernameToken11", Constants.SP_PREFIX);

    public static final QName ENCRYPTED_PARTS = new QName(Constants.SP_NS,
            "EncryptedParts", Constants.SP_PREFIX);

    public static final QName SIGNED_ELEMENTS = new QName(Constants.SP_NS,
            "SignedElements", Constants.SP_PREFIX);

    public static final QName ENCRYPTED_ELEMENTS = new QName(Constants.SP_NS,
            "EncryptedElements", Constants.SP_PREFIX);

    public static final QName ENCRYPTION_TOKEN = new QName(Constants.SP_NS,
            "EncryptionToken", Constants.SP_PREFIX);

    public static final QName X509_TOKEN = new QName(Constants.SP_NS,
            "X509Token", Constants.SP_PREFIX);

    public static final QName ISSUED_TOKEN = new QName(Constants.SP_NS,
            "IssuedToken", Constants.SP_PREFIX);

    public static final QName SUPPORIING_TOKENS = new QName(Constants.SP_NS,
            "SupportingTokens", Constants.SP_PREFIX);

    public static final QName SIGNED_SUPPORTING_TOKENS = new QName(
            Constants.SP_NS, "SignedSupportingTokens", Constants.SP_PREFIX);

    public static final QName ENDORSING_SUPPORTING_TOKENS = new QName(
            Constants.SP_NS, "EndorsingSupportingTokens", Constants.SP_PREFIX);

    public static final QName SIGNED_ENDORSING_SUPPORTING_TOKENS = new QName(
            Constants.SP_NS, "SignedEndorsingSupportingTokens",
            Constants.SP_PREFIX);

    public static final QName PROTECTION_TOKEN = new QName(Constants.SP_NS,
            "ProtectionToken", Constants.SP_PREFIX);

    public static final QName ASYMMETRIC_BINDING = new QName(Constants.SP_NS,
            "AsymmetricBinding", Constants.SP_PREFIX);

    public static final QName SYMMETRIC_BINDING = new QName(Constants.SP_NS,
            "SymmetricBinding", Constants.SP_PREFIX);

    public static final QName INITIATOR_TOKEN = new QName(Constants.SP_NS,
            "InitiatorToken", Constants.SP_PREFIX);

    public static final QName RECIPIENT_TOKEN = new QName(Constants.SP_NS,
            "RecipientToken", Constants.SP_PREFIX);

    public static final QName ENCRYPT_SIGNATURE = new QName(Constants.SP_NS,
            "EncryptSignature", Constants.SP_PREFIX);

    public static final QName PROTECT_TOKENS = new QName(Constants.SP_NS,
            "ProtectTokens", Constants.SP_PREFIX);

    public static final QName REQUIRE_KEY_IDENTIFIRE_REFERENCE = new QName(
            Constants.SP_NS, "RequireKeyIdentifireReference",
            Constants.SP_PREFIX);

    public static final QName REQUIRE_ISSUER_SERIAL_REFERENCE = new QName(
            Constants.SP_NS, "RequireIssuerSerializerReference",
            Constants.SP_PREFIX);

    public static final QName REQUIRE_EMBEDDED_TOKEN_REFERENCE = new QName(
            Constants.SP_NS, "RequireEmbeddedTokenReference",
            Constants.SP_PREFIX);

    public static final QName REQUIRE_THUMBPRINT_REFERENCE = new QName(
            Constants.SP_NS, "RequireThumbprintReference", Constants.SP_PREFIX);

    public static final QName WSS_X509_V1_TOKEN_10 = new QName(Constants.SP_NS,
            "WssX509V1Token10", Constants.SP_PREFIX);

    public static final QName WSS_X509_V3_TOKEN_10 = new QName(Constants.SP_NS,
            "WssX509V3Token10", Constants.SP_PREFIX);

    public static final QName WSS_X509_PKCS7_TOKEN_10 = new QName(
            Constants.SP_NS, "WssX509Pkcs7Token10", Constants.SP_PREFIX);

    public static final QName WSS_X509_PKI_PATH_V1_TOKEN_10 = new QName(
            Constants.SP_NS, "WssX509PkiPathV1Token10", Constants.SP_PREFIX);

    public static final QName WSS_X509_V1_TOKEN_11 = new QName(Constants.SP_NS,
            "WssX509V1Token11", Constants.SP_PREFIX);

    public static final QName WSS_X509_V3_TOKEN_11 = new QName(Constants.SP_NS,
            "WssX509V3Token11", Constants.SP_PREFIX);

    public static final QName WSS_X509_PKCS7_TOKEN_11 = new QName(
            Constants.SP_NS, "WssX509Pkcs7Token11", Constants.SP_PREFIX);

    public static final QName WSS_X509_PKI_PATH_V1_TOKEN_11 = new QName(
            Constants.SP_NS, "WssX509PkiPathV1Token11", Constants.SP_PREFIX);

    public static final QName MUST_SUPPORT_REF_KEY_IDENTIFIER = new QName(
            Constants.SP_NS, "MustSupportRefKeyIdentifier", Constants.SP_PREFIX);

    public static final QName MUST_SUPPORT_REF_ISSUER_SERIAL = new QName(
            Constants.SP_NS, "MustSupportRefIssuerSerial", Constants.SP_PREFIX);

    public static final QName MUST_SUPPORT_REF_EXTERNAL_URI = new QName(
            Constants.SP_NS, "MustSupportRefExternalURI", Constants.SP_PREFIX);

    public static final QName MUST_SUPPORT_REF_EMBEDDED_TOKEN = new QName(
            Constants.SP_NS, "MustSupportRefEmbeddedToken", Constants.SP_PREFIX);

    public static final QName WSS10 = new QName(Constants.SP_NS, "Wss10",
            Constants.SP_PREFIX);

    public static final QName WSS11 = new QName(Constants.SP_NS, "Wss11",
            Constants.SP_PREFIX);

    public static final QName TRUST_10 = new QName(Constants.SP_NS, "Trust10",
            Constants.SP_PREFIX);

    public static final QName MUST_SUPPORT_REF_THUMBPRINT = new QName(
            Constants.SP_NS, "MustSupportRefThumbprint", Constants.SP_PREFIX);

    public static final QName MUST_SUPPORT_REF_ENCRYPTED_KEY = new QName(
            Constants.SP_NS, "MustSupportRefEncryptedkey", Constants.SP_PREFIX);

    public static final QName REQUIRE_SIGNATURE_CONFIRMATION = new QName(
            Constants.SP_NS, "RequireSignatureConfirmation",
            Constants.SP_PREFIX);

    public static final QName MUST_SUPPORT_CLIENT_CHALLENGE = new QName(
            Constants.SP_NS, "MustSupportClientChanllenge", Constants.SP_PREFIX);

    public static final QName MUST_SUPPORT_SERVER_CHALLENGE = new QName(
            Constants.SP_NS, "MustSupportServerChanllenge", Constants.SP_PREFIX);

    public static final QName REQUIRE_CLIENT_ENTROPY = new QName(
            Constants.SP_NS, "RequireClientEntropy", Constants.SP_PREFIX);

    public static final QName REQUIRE_SERVER_ENTROPY = new QName(
            Constants.SP_NS, "RequireServerEntropy", Constants.SP_PREFIX);

    public static final QName MUST_SUPPORT_ISSUED_TOKENS = new QName(
            Constants.SP_NS, "MustSupportIssuedTokens", Constants.SP_PREFIX);

    public static final QName ISSUER = new QName(Constants.SP_NS, "Issuer",
            Constants.SP_PREFIX);

    public static final QName REQUIRE_DERIVED_KEYS = new QName(Constants.SP_NS,
            "RequireDerivedKeys", Constants.SP_PREFIX);

    public static final QName REQUIRE_EXTERNAL_URI_REFERNCE = new QName(
            Constants.SP_NS, "RequireExternalUriReference", Constants.SP_PREFIX);

    public static final QName REQUIRE_EXTERNAL_REFERNCE = new QName(
            Constants.SP_NS, "RequireExternalReference", Constants.SP_PREFIX);

    public static final QName REQUIRE_INTERNAL_REFERNCE = new QName(
            Constants.SP_NS, "RequireInternalReference", Constants.SP_PREFIX);

    public static final QName REQUEST_SECURITY_TOKEN_TEMPLATE = new QName(
            Constants.SP_NS, "RequestSecurityTokenTemplate",
            Constants.SP_PREFIX);

    public static final QName SC10_SECURITY_CONTEXT_TOKEN = new QName(
            Constants.SP_NS, "SC10SecurityContextToken", Constants.SP_PREFIX);

    public static final QName BOOTSTRAP_POLICY = new QName(Constants.SP_NS,
            "BootstrapPolicy", Constants.SP_PREFIX);

    public static final QName RST_TEMPLATE = new QName(Constants.SP_NS,
            "RequestSecurityTokenTemplate", Constants.SP_PREFIX);

    public final static QName REQUIRE_CLIENT_CERTIFICATE = new QName(
            "RequireClientCertificate");

    public final static QName XPATH_ = new QName(Constants.SP_NS, "XPath",
            Constants.SP_PREFIX);
}
