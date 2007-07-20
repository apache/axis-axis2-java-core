package org.apache.axis2.tools.idea;


import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * this class use for filter file
 */
public class WSDLFileFilter extends FileFilter {

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        if (extension != null) {
            return extension.equals("wsdl");
        }

        return false;

    }

    public String getDescription() {
        return ".wsdl";
    }

    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

}