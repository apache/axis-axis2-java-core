package org.apache.axis2.tools.java2wsdl;


public class FileFilter {

    public boolean accept(String fileName ) {

        String   extension = getExtension(fileName);
        if (extension != null && extension.equals("wsdl")) {
            return  true;
        }else if(extension != null && extension.equals("xml")) {
            return true;
        }
        return false;
    }

    public String getDescription() {
        return ".wsdl";
    }

    private String getExtension(String  extension) {
        String ext = null;
        int i = extension.lastIndexOf('.');

        if (i > 0 && i < extension.length() - 1) {
            ext = extension.substring(i + 1).toLowerCase();
        }
        return ext;
    }

}
