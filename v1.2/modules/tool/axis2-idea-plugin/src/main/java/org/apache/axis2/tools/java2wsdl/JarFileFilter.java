package org.apache.axis2.tools.java2wsdl;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class JarFileFilter extends FileFilter {

    public boolean accept(File file) {
        if(file.isDirectory() ){
            return true;
        }
        String extension = getExtension(file);
        if(extension != null){
            return  extension .equals("jar");
        }
        return false;

    }

    public String getDescription() {
        return ".jar" ;
    }

    private String getExtension(File file){
        String ext = null;
        String s = file.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
}

