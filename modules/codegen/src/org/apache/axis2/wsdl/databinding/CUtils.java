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

package org.apache.axis2.wsdl.databinding;

import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

public class CUtils {

    static final String keywords[] =
            {
                    "auto", "double", "int", "struct",
                    "break", "else", "long", "switch",
                    "case", "enum", "register", "typedef",
                    "char", "extern", "return", "union",
                    "const", "float", "short", "unsigned",
                    "continue", "for", "signed", "void",
                    "default", "goto", "sizeof", "volatile",
                    "do", "if", "static", "while"
            };

    /** Collator for comparing the strings */
    static final Collator englishCollator = Collator.getInstance(Locale.ENGLISH);

    /** Use this character as suffix */
    static final char keywordPrefix = '_';


    /**
     * Checks if the input string is a valid C keyword.
     *
     * @return Returns boolean.
     */
    public static boolean isCKeyword(String keyword) {
        return (Arrays.binarySearch(keywords, keyword, englishCollator) >= 0);
    }

    /**
     * Turns a C keyword string into a non-C keyword string.  (Right now this simply means appending
     * an underscore.)
     */
    public static String makeNonCKeyword(String keyword) {
        return keywordPrefix + keyword;
    }


}
