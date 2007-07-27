package org.apache.ideaplugin.bean;

import javax.swing.filechooser.FileFilter;
import java.io.File;


public class ClassFileFilter extends FileFilter {

    public boolean accept(File file) {
        if (file .isDirectory()) {
            return true;
        }
        String extension = getExtension(file );
        if (extension != null) {
            return extension.equals("class");
        }

        return false;

    }

    public String getDescription() {
        return ".class";
    }

    private String getExtension(File file) {
        String ext = null;
        String s = file.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
}
