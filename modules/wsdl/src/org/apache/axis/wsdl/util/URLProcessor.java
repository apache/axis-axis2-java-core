package org.apache.axis.wsdl.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
 *
 * 
 */
public class URLProcessor {
    public static final String DEFAULT_PACKAGE = "axis2";
    public static String getNameSpaceFromURL(String url){
           String returnPackageName = "";
           String regularExpression = "//[\\w\\.]*";
           Pattern urlBreaker =Pattern.compile(regularExpression);
           Matcher matcher = urlBreaker.matcher(url);
           if (matcher.find()) {
               String s = matcher.group();
               s = s.replaceAll("//","");
               String[] arrayOfItems = s.split("\\.");
               int length = arrayOfItems.length;
               for (int i = length; i > 0; i--) {
                   returnPackageName = returnPackageName.concat((i==length?"":".") + arrayOfItems[i-1]);
               }
           }else{
               returnPackageName = DEFAULT_PACKAGE;
           }

           return returnPackageName;
       }

}
