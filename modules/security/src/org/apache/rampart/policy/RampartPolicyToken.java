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

package org.apache.rampart.policy;

import java.util.ArrayList;

/**
 * 
 * This class holds data and parameters for a specific token. The
 * signedParts/Elements and encryptedParts/Elements lists hold additional
 * information in case of supporting tokens.
 * 
 * <p/>
 * 
 * The data is not declared as private to provide direct access from
 * other classes in this package.
 */
public class RampartPolicyToken {
	
	public static final int X509_TOKEN = 1;
    
    public static final int USERNAME_TOKEN = 2;
	
	int tokenType;
	
	String sigAlgorithm;

	int keyIdentifier;

	String encAlgorithm;

	String encTransportAlgorithm;

	ArrayList sigParts;

	ArrayList sigElements;

	ArrayList encParts;

	ArrayList encElements;

    boolean useDerivedKeys;
    
	/**
     * @return Returns the useDerivedKeys.
     */
    public boolean isUseDerivedKeys() {
        return useDerivedKeys;
    }

    /**
	 * @return Returns the tokenType.
	 */
	public int getTokenType() {
		return tokenType;
	}

	/**
	 * @return Returns the encAlgorithm.
	 */
	public String getEncAlgorithm() {
		return encAlgorithm;
	}

	/**
	 * @return Returns the encElements.
	 */
	public ArrayList getEncElements() {
		return encElements;
	}

	/**
	 * @return Returns the encKeyIdentifier.
	 */
	public int getKeyIdentifier() {
		return keyIdentifier;
	}

	/**
	 * @return Returns the encParts.
	 */
	public ArrayList getEncParts() {
		return encParts;
	}

	/**
	 * @return Returns the encTransportAlgorithm.
	 */
	public String getEncTransportAlgorithm() {
		return encTransportAlgorithm;
	}

	/**
	 * @return Returns the sigAlgorithm.
	 */
	public String getSigAlgorithm() {
		return sigAlgorithm;
	}

	/**
	 * @return Returns the sigElements.
	 */
	public ArrayList getSigElements() {
		return sigElements;
	}

	/**
	 * @return Returns the sigParts.
	 */
	public ArrayList getSigParts() {
		return sigParts;
	}
}
