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
 *  Runtime state of the engine
 */
package org.apache.axis.util;

public class URL {
    private String protocol;
    private String host;
    private int port = -1;
    private String fileName;
    
    public URL(String url){
        int start = 0;
        int end = 0;
        end = url.indexOf("://");
        if(end > 0){
            protocol = url.substring(0,end);
            start = end + 3;
        }
        
        end = url.indexOf('/',start);
        if(end > 0){
            String hostAndPort = url.substring(start,end);
            fileName =  url.substring(end);
            int index = hostAndPort.indexOf(':');
            if(index>0){
                host = hostAndPort.substring(0,index);
                port = Integer.parseInt(hostAndPort.substring(index+1));
            }else{
                host = hostAndPort;
            }
        }else{
            host = url;
        }
         
        
    }
    /**
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * @return
     */
    public String getProtocol() {
        return protocol;
    }

}
