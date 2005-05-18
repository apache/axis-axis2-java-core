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
package org.apache.axis.om;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author srinath
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface DataSource {
    /**
     * @return
     */
    public abstract String getContentType();
    /**
     * @return
     * @throws java.io.IOException
     */
    public abstract InputStream getInputStream() throws IOException;
    /**
     * @return
     */
    public abstract String getName();
    /**
     * @return
     * @throws java.io.IOException
     */
    public abstract OutputStream getOutputStream() throws IOException;
}