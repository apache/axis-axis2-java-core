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
package org.apache.axis.om.impl.llom.mtom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.axis.om.DataSource;




public class DataSourceImpl implements DataSource {
    
    
    private javax.activation.DataSource datasource;
    
    public DataSourceImpl(javax.activation.DataSource datasource){
        this.datasource =datasource;
    }
    
    /**
     * @return
     */
    public String getContentType() {
        return datasource.getContentType();
    }

    /**
     * @return
     * @throws java.io.IOException
     */
    public InputStream getInputStream() throws IOException {
        return datasource.getInputStream();
    }

    /**
     * @return
     */
    public String getName() {
        return datasource.getName();
    }

    /**
     * @return
     * @throws java.io.IOException
     */
    public OutputStream getOutputStream() throws IOException {
        return datasource.getOutputStream();
    }

}
