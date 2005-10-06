/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.axis2.databinding.toJava;

import java.io.IOException;

/**
 * Class DuplicateFileException
 * 
 * @version %I%, %G%
 */
public class DuplicateFileException extends IOException {

    /** Field filename */
    private String filename = null;

    /**
     * Constructor DuplicateFileException
     * 
     * @param message  
     * @param filename 
     */
    public DuplicateFileException(String message, String filename) {

        super(message);

        this.filename = filename;
    }

    /**
     * Method getFileName
     * 
     * @return 
     */
    public String getFileName() {
        return this.filename;
    }
}
