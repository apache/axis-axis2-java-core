package org.apache.axis2.databinding.schema;
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

/**
 * This is a class used as a holder to pass on the meta information to the bean writer
 * This meta information will be used by the writer to write the databinding conversion code
 */
public class BeanWriterMetaInfoHolder {

    private boolean ordered = false;
    private boolean extension = false;
    private String extensionClassName = "";

    public String getExtensionClassName() {
        return extensionClassName;
    }

    public void setExtensionClassName(String extensionClassName) {
        this.extensionClassName = extensionClassName;
    }

    public boolean isExtension() {
        return extension;
    }

    public void setExtension(boolean extension) {
        this.extension = extension;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }


}
