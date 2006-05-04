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
package org.apache.ws.security.policy.parser;

public class SecurityPolicy {

	public static final SecurityPolicyToken signedParts = new SecurityPolicyToken("SignedParts",
			SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken header = new SecurityPolicyToken("Header",
			SecurityPolicyToken.SIMPLE_TOKEN, new String[] { "Name",
					"Namespace" });

	public static final SecurityPolicyToken body = new SecurityPolicyToken("Body",
			SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken signedElements = new SecurityPolicyToken(
			"SignedElements", SecurityPolicyToken.COMPLEX_TOKEN,
			new String[] { "XPathVersion" });

	public static final SecurityPolicyToken xPath = new SecurityPolicyToken(
			"XPath",
			SecurityPolicyToken.SIMPLE_TOKEN | SecurityPolicyToken.WITH_CONTENT,
			null);

	public static final SecurityPolicyToken encryptedParts = new SecurityPolicyToken(
			"EncryptedParts", SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken encryptedElements = new SecurityPolicyToken(
			"EncryptedElements", SecurityPolicyToken.COMPLEX_TOKEN,
			new String[] { "XPathVersion" });

	public static final SecurityPolicyToken requiredElements = new SecurityPolicyToken(
			"RequiredElements", SecurityPolicyToken.COMPLEX_TOKEN,
			new String[] { "XPathVersion" });

	public static final SecurityPolicyToken usernameToken = new SecurityPolicyToken(
			"UsernameToken", SecurityPolicyToken.COMPLEX_TOKEN,
			new String[] { "IncludeToken" });

	public static final SecurityPolicyToken wssUsernameToken10 = new SecurityPolicyToken(
			"WssUsernameToken10", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssUsernameToken11 = new SecurityPolicyToken(
			"WssUsernameToken11", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken issuedToken = new SecurityPolicyToken("IssuedToken",
			SecurityPolicyToken.COMPLEX_TOKEN, new String[] { "IncludeToken" });

	public static final SecurityPolicyToken issuer = new SecurityPolicyToken(
			"Issuer",
			SecurityPolicyToken.SIMPLE_TOKEN | SecurityPolicyToken.WITH_CONTENT,
			null);

	public static final SecurityPolicyToken requestSecurityTokenTemplate = new SecurityPolicyToken(
			"RequestSecurityTokenTemplate", SecurityPolicyToken.COMPLEX_TOKEN
					| SecurityPolicyToken.WITH_CONTENT,
			new String[] { "TrustVersion" });

	public static final SecurityPolicyToken requireDerivedKeys = new SecurityPolicyToken(
			"RequireDerivedKeys", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken requireExternalReference = new SecurityPolicyToken(
			"RequireExternalReference", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken requireInternalReference = new SecurityPolicyToken(
			"RequireInternalReference", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken x509Token = new SecurityPolicyToken("X509Token",
			SecurityPolicyToken.COMPLEX_TOKEN, new String[] { "IncludeToken" });

	public static final SecurityPolicyToken requireKeyIdentifierReference = new SecurityPolicyToken(
			"RequireKeyIdentifierReference", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken requireIssuerSerialReference = new SecurityPolicyToken(
			"RequireIssuerSerialReference", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken requireEmbeddedTokenReference = new SecurityPolicyToken(
			"RequireEmbeddedTokenReference", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken requireThumbprintReference = new SecurityPolicyToken(
			"RequireThumbprintReference", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken wssX509V1Token10 = new SecurityPolicyToken(
			"WssX509V1Token10", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssX509V3Token10 = new SecurityPolicyToken(
			"WssX509V3Token10", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssX509Pkcs7Token10 = new SecurityPolicyToken(
			"WssX509Pkcs7Token10", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssX509PkiPathV1Token10 = new SecurityPolicyToken(
			"WssX509PkiPathV1Token10", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssX509V1Token11 = new SecurityPolicyToken(
			"WssX509V1Token11", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssX509V3Token11 = new SecurityPolicyToken(
			"WssX509V3Token11", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssX509Pkcs7Token11 = new SecurityPolicyToken(
			"WssX509Pkcs7Token11", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssX509PkiPathV1Token11 = new SecurityPolicyToken(
			"WssX509PkiPathV1Token11", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken kerberosToken = new SecurityPolicyToken(
			"KerberosToken", SecurityPolicyToken.COMPLEX_TOKEN,
			new String[] { "IncludeToken" });

	// requireDerivedKeys already defined for issuedToken
	// requireKeyIdentifierReference already defined for x509Token
	public static final SecurityPolicyToken wssKerberosV5ApReqToken11 = new SecurityPolicyToken(
			"WssKerberosV5ApReqToken11", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssGssKerberosV5ApReqToken11 = new SecurityPolicyToken(
			"WssGssKerberosV5ApReqToken11", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken spnegoContextToken = new SecurityPolicyToken(
			"SpnegoContextToken", SecurityPolicyToken.COMPLEX_TOKEN,
			new String[] { "IncludeToken" });

	// issuer already defined for issuedToken
	// requireDerivedKeys already defined for issuedToken

	public static final SecurityPolicyToken securityContextToken = new SecurityPolicyToken(
			"SecurityContextToken", SecurityPolicyToken.COMPLEX_TOKEN,
			new String[] { "IncludeToken" });

	// requireDerivedKeys already defined for issuedToken
	public static final SecurityPolicyToken requireExternalUriReference = new SecurityPolicyToken(
			"RequireExternalUriReference", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken sc10SecurityContextToken = new SecurityPolicyToken(
			"SC10SecurityContextToken", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken secureConversationToken = new SecurityPolicyToken(
			"SecureConversationToken", SecurityPolicyToken.COMPLEX_TOKEN,
			new String[] { "IncludeToken" });

	// issuer already defined for issuedToken
	// requireDerivedKeys already defined for issuedToken
	// requireExternalUriReference is already defined for SecurityContextToken
	// sc10SecurityContextToken is already defined for SecurityContextToken
	public static final SecurityPolicyToken bootstrapPolicy = new SecurityPolicyToken(
			"BootstrapPolicy", SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken samlToken = new SecurityPolicyToken("SamlToken",
			SecurityPolicyToken.COMPLEX_TOKEN, new String[] { "IncludeToken" });

	// requireDerivedKeys already defined for issuedToken
	// requireKeyIdentifierReference already defined for x509Token
	public static final SecurityPolicyToken wssSamlV10Token10 = new SecurityPolicyToken(
			"WssSamlV10Token10", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssSamlV11Token10 = new SecurityPolicyToken(
			"WssSamlV11Token10", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssSamlV10Token11 = new SecurityPolicyToken(
			"WssSamlV10Token11", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssSamlV11Token11 = new SecurityPolicyToken(
			"WssSamlV11Token11", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssSamlV20Token11 = new SecurityPolicyToken(
			"WssSamlV20Token11", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken relToken = new SecurityPolicyToken("RelToken",
			SecurityPolicyToken.COMPLEX_TOKEN, new String[] { "IncludeToken" });

	// requireDerivedKeys already defined for issuedToken
	// requireKeyIdentifierReference already defined for x509Token
	public static final SecurityPolicyToken wssRelV10Token10 = new SecurityPolicyToken(
			"WssRelV10Token10", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssRelV20Token10 = new SecurityPolicyToken(
			"WssRelV20Token10", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssRelV10Token11 = new SecurityPolicyToken(
			"WssRelV10Token11", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken wssRelV20Token11 = new SecurityPolicyToken(
			"WssRelV20Token11", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken httpsToken = new SecurityPolicyToken("HttpsToken",
			SecurityPolicyToken.COMPLEX_TOKEN,
			new String[] { "RequireClientCertificate" });

	public static final SecurityPolicyToken algorithmSuite = new SecurityPolicyToken("AlgorithmSuite",
			SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken basic256 = new SecurityPolicyToken("Basic256",
			SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken basic192 = new SecurityPolicyToken("Basic192",
			SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken basic128 = new SecurityPolicyToken("Basic128",
			SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken tripleDes = new SecurityPolicyToken("TripleDes",
			SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken basic256Rsa15 = new SecurityPolicyToken(
			"Basic256Rsa15", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken basic192Rsa15 = new SecurityPolicyToken(
			"Basic192Rsa15", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken basic128Rsa15 = new SecurityPolicyToken(
			"Basic128Rsa15", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken tripleDesRsa15 = new SecurityPolicyToken(
			"TripleDesRsa15", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken basic256Sha256 = new SecurityPolicyToken(
			"Basic256Sha256", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken basic192Sha256 = new SecurityPolicyToken(
			"Basic192Sha256", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken basic128Sha256 = new SecurityPolicyToken(
			"Basic128Sha256", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken tripleDesSha256 = new SecurityPolicyToken(
			"TripleDesSha256", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken basic256Sha256Rsa15 = new SecurityPolicyToken(
			"Basic256Sha256Rsa15", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken basic192Sha256Rsa15 = new SecurityPolicyToken(
			"Basic192Sha256Rsa15", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken basic128Sha256Rsa15 = new SecurityPolicyToken(
			"Basic128Sha256Rsa15", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken tripleDesSha256Rsa15 = new SecurityPolicyToken(
			"TripleDesSha256Rsa15", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken inclusiveC14N = new SecurityPolicyToken(
			"InclusiveC14N", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken soapNormalization10 = new SecurityPolicyToken(
			"SoapNormalization10", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken strTransform10 = new SecurityPolicyToken(
			"StrTransform10", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken xPath10 = new SecurityPolicyToken("XPath10",
			SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken xPathFilter20 = new SecurityPolicyToken(
			"XPathFilter20", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken layout = new SecurityPolicyToken("Layout",
			SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken strict = new SecurityPolicyToken("Strict",
			SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken lax = new SecurityPolicyToken("Lax",
			SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken laxTsFirst = new SecurityPolicyToken("LaxTsFirst",
			SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken laxTsLast = new SecurityPolicyToken("LaxTsLast",
			SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken transportBinding = new SecurityPolicyToken(
			"TransportBinding", SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken transportToken = new SecurityPolicyToken(
			"TransportToken", SecurityPolicyToken.COMPLEX_TOKEN, null);

	// algorithmSuite and layout see above
	public static final SecurityPolicyToken includeTimestamp = new SecurityPolicyToken(
			"IncludeTimestamp", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken symmetricBinding = new SecurityPolicyToken(
			"SymmetricBinding", SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken encryptionToken = new SecurityPolicyToken(
			"EncryptionToken", SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken signatureToken = new SecurityPolicyToken(
			"SignatureToken", SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken protectionToken = new SecurityPolicyToken(
			"ProtectionToken", SecurityPolicyToken.COMPLEX_TOKEN, null);

	// algorithmSuite and layout see above
	// includeTimestamp already defined for transport binding
	public static final SecurityPolicyToken encryptBeforeSigning = new SecurityPolicyToken(
			"EncryptBeforeSigning", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken encryptSignature = new SecurityPolicyToken(
			"EncryptSignature", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken protectTokens = new SecurityPolicyToken(
			"ProtectTokens", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken onlySignEntireHeadersAndBody = new SecurityPolicyToken(
			"OnlySignEntireHeadersAndBody", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken asymmetricBinding = new SecurityPolicyToken(
			"AsymmetricBinding", SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken initiatorToken = new SecurityPolicyToken(
			"InitiatorToken", SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken recipientToken = new SecurityPolicyToken(
			"RecipientToken", SecurityPolicyToken.COMPLEX_TOKEN, null);

	// all other tokens for asymmetric already defined above

	public static final SecurityPolicyToken supportingTokens = new SecurityPolicyToken(
			"SupportingTokens", SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken signedSupportingTokens = new SecurityPolicyToken(
			"SignedSupportingTokens", SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken endorsingSupportingTokens = new SecurityPolicyToken(
			"EndorsingSupportingTokens", SecurityPolicyToken.COMPLEX_TOKEN,
			null);

	public static final SecurityPolicyToken signedEndorsingSupportingTokens = new SecurityPolicyToken(
			"SignedEndorsingSupportingTokens",
			SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken wss10 = new SecurityPolicyToken("Wss10",
			SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken mustSupportRefKeyIdentifier = new SecurityPolicyToken(
			"MustSupportRefKeyIdentifier", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken mustSupportRefIssuerSerial = new SecurityPolicyToken(
			"MustSupportRefIssuerSerial", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken mustSupportRefExternalUri = new SecurityPolicyToken(
			"MustSupportRefExternalURI", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken mustSupportRefEmbeddedToken = new SecurityPolicyToken(
			"MustSupportRefEmbeddedToken", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken wss11 = new SecurityPolicyToken("Wss11",
			SecurityPolicyToken.COMPLEX_TOKEN, null);

	// all from wss10
	public static final SecurityPolicyToken mustSupportRefThumbprint = new SecurityPolicyToken(
			"MustSupportRefThumbprint", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken mustSupportRefEncryptedKey = new SecurityPolicyToken(
			"MustSupportRefEncryptedKey", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken requireSignatureConfirmation = new SecurityPolicyToken(
			"RequireSignatureConfirmation", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken trust10 = new SecurityPolicyToken("Trust10",
			SecurityPolicyToken.COMPLEX_TOKEN, null);

	public static final SecurityPolicyToken mustSupportClientChallenge = new SecurityPolicyToken(
			"MustSupportClientChallenge", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken mustSupportServerChallenge = new SecurityPolicyToken(
			"MustSupportServerChallenge", SecurityPolicyToken.SIMPLE_TOKEN,
			null);

	public static final SecurityPolicyToken requireClientEntropy = new SecurityPolicyToken(
			"RequireClientEntropy", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken requireServerEntropy = new SecurityPolicyToken(
			"RequireServerEntropy", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final SecurityPolicyToken mustSupportIssuedTokens = new SecurityPolicyToken(
			"MustSupportIssuedTokens", SecurityPolicyToken.SIMPLE_TOKEN, null);

	public static final String includeNever = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Never";

	public static final String includeOnce = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Once";

	public static final String includeAlways = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Always";

}
