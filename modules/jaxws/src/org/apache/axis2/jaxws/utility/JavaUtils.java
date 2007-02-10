/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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

package org.apache.axis2.jaxws.utility;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Common Java Utilites
 *
 */
public class JavaUtils extends org.apache.axis2.util.JavaUtils {
    
    /**
     * Private Constructor...All methods of this class are static
     */
    private JavaUtils() {}
    
    /**
     * Namespace 2 Package algorithm as defined by the JAXB Specification
     * @param Namespace
     * @return String represeting Namespace
     */
    public static String getPackageFromNamespace(String namespace) {
        // The following steps correspond to steps described in the JAXB Specification
        
        // Step 1: Scan off the host name
        String hostname = null;
        String path = null;
        try {
        	URL url = new URL(namespace);
            hostname = url.getHost();
            path = url.getPath();
        }
        catch (MalformedURLException e) {
               // No FFDC code needed
            if (namespace.indexOf(":") > -1) {
                // Brain-dead code to skip over the protocol
                hostname = namespace.substring(namespace.indexOf(":") + 1);
            }
            else {
                hostname = namespace;
            }
        }
        
        // Step 3: Tokenize the host name using ":" and "/"
        StringTokenizer st = new StringTokenizer( hostname, ":/" );
        
        ArrayList<String> wordList = new ArrayList<String>();
        
        //Read Hostname first.
        for(int i = 0; st !=null &&  i < st.countTokens(); ++i) {
        	wordList.add(st.nextToken());
        }
        //Read rest Of the path now
        if(path!=null){
	        StringTokenizer pathst = new StringTokenizer(path,"/");
	        while(pathst!=null && pathst.hasMoreTokens()){
	        	wordList.add(pathst.nextToken());
	        }
        }
        String[] words = wordList.toArray(new String[0]);
        
        // Now do step 2: Strip off the trailing "." (i.e. strip off .html)
        if(words !=null && words.length > 1 ){
	        String lastWord = words[words.length-1];
	        int index = lastWord.lastIndexOf('.');
	        if (index > 0) {
	            words[words.length-1] = lastWord.substring(0,index);
	        }
        }
        
        
        // Step 4: Unescape each escape sequence
        // TODO I don't know what to do here.
        
        // Step 5: If protocol is urn, replace - with . in the first word
        if (namespace.startsWith("urn:")) {
            words[0] = replace(words[0], "-", ".");
        }
        
        // Step 6: Tokenize the first word with "." and reverse the order. (the www is also removed).
        // TODO This is not exactly equivalent to the JAXB Rule.
        StringTokenizer st2 = new StringTokenizer(words[0], ".");
        ArrayList<String> list = new ArrayList<String>();
        while(st2.hasMoreTokens()) {
            // Add the strings so they are in reverse order
            list.add(0,st2.nextToken());
        }
        // Remove www
        String last = list.get(list.size()-1);
        if (last.equals("www")) {
            list.remove(list.size()-1);
        }
        // Now each of words is represented by list
        for (int i=1; i<words.length; i++) {
            list.add(words[i]);
        }
        
        // Step 7: lowercase each word
        for (int i =0; i<list.size(); i++) {
            String word = list.remove(i);
            word = word.toLowerCase();
            list.add(i, word);
        }
        
        // Step 8: make into and an appropriate java word
        for (int i =0; i<list.size(); i++) {
            String word = list.get(i);

            // 8a: Convert special characters to underscore
            // Convert non-java words to underscore.
            // TODO: Need to do this for all chars..not just hyphens
            word = replace(word, "-", "_");
            
            // 8b: Append _ to java keywords
            if (JavaUtils.isJavaKeyword(word)) {
                word = word + "_";
            }
            // 8c: prepend _ if first character cannot be the first character of a java identifier
            if (!Character.isJavaIdentifierPart(word.charAt(0)) ) {
                word = "_" + word;
            }
            
            list.set(i, word);
        }
        
        // Step 9: Concatenate and return
        String name = "";
        for (int i =0; i<list.size(); i++) {
            if (i == 0) {
                name = list.get(0);
            } else {
                name =name + "." + list.get(i);
            }
        }
        return name;
    }
    
    /**
     * replace:
     * Like String.replace except that the old new items are strings.
     *
     * @param name string
     * @param oldT old text to replace
     * @param newT new text to use
     * @return replacement string
     **/
    public static final String replace (String name,
                                        String oldT, String newT) {

        if (name == null) return "";

        // Create a string buffer that is twice initial length.
        // This is a good starting point.
        StringBuffer sb = new StringBuffer(name.length()* 2);

        int len = oldT.length ();
        try {
            int start = 0;
            int i = name.indexOf (oldT, start);

            while (i >= 0) {
                sb.append(name.substring(start, i));
                sb.append(newT);
                start = i+len;
                i = name.indexOf(oldT, start);
            }
            if (start < name.length())
                sb.append(name.substring(start));
        } catch (NullPointerException e) {
               // No FFDC code needed
        }

        return new String(sb);
    }

    /**
     * Get a string containing the stack of the current location
     * @return String
     */
    public static String stackToString(){
        return stackToString(new RuntimeException());
      }
    
    /**
     * Get a string containing the stack of the specified exception
     * @param e
     * @return
     */
    public static String stackToString(Throwable e) {
        java.io.StringWriter sw= new java.io.StringWriter(); 
        java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);
        java.io.PrintWriter pw= new java.io.PrintWriter(bw); 
        e.printStackTrace(pw);
        pw.close();
        String text = sw.getBuffer().toString();
        // Jump past the throwable
        text = text.substring(text.indexOf("at"));
        text = replace(text, "at ", "DEBUG_FRAME = ");
        return text;
    }
}
