package org.apache.axis.tool.core;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

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

public class FileCopier extends Copy{
    public FileCopier() {
        this.setProject(new Project());
        this.getProject().init();
        this.setTaskType("copy");
        this.setTaskName("copy-files");
        this.setOwningTarget(new org.apache.tools.ant.Target());
    }

    public void copyFiles(File sourceFile,File destinationDirectory){

        this.filesets.clear();

        if (sourceFile.isFile())
            this.setFile(sourceFile);
        else {
            FileSet fileset = new FileSet();
            fileset.setDir(sourceFile);
            fileset.setIncludes("*/**");
            this.addFileset(fileset);
        }
        this.setTodir(destinationDirectory);
        this.perform();
    }


}
