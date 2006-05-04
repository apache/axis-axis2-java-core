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
package org.apache.axis2;

/**
 * 
 */
public final class HTTPConstants {
    public static final String MEDIA_TYPE_X_WWW_FORM = "application/x-www-form-urlencoded";
    public static final String MEDIA_TYPE_APPLICATION_XML = "application/xml";
    public static final String MEDIA_TYPE_TEXT_XML = "text/xml";
    public static final String MEDIA_TYPE_MULTIPART_RELATED = "multipart/related";

    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD = "HTTP_METHOD";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String CONTENT_TYPE = "ContentType";

    private HTTPConstants() {
    }
}
