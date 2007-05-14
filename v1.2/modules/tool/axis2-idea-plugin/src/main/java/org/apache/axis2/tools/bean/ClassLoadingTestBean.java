package org.apache.axis2.tools.bean;

import java.util.List;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.io.File;

public class ClassLoadingTestBean {

    public static boolean tryLoadingClass(String className,
                                          String[] classPathEntries, List errorListener) {

        //make a URL class loader from the entries
        ClassLoader classLoader;

        if (classPathEntries.length > 0) {
            URL[] urls = new URL[classPathEntries.length];

            try {
                for (int i = 0; i < classPathEntries.length; i++) {
                    String classPathEntry = classPathEntries[i];
                    //this should be a file(or a URL)
                    if (classPathEntry.startsWith("http://")) {
                        urls[i] = new URL(classPathEntry);
                    } else {
                        urls[i] = new File(classPathEntry).toURL();
                    }
                }
            } catch (MalformedURLException e) {
                if (errorListener!=null){
                    errorListener.add(e);
                }
                return false;
            }

            classLoader = new URLClassLoader(urls);

        } else {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        //try to load the class with the given name

        try {
            Class clazz=classLoader.loadClass(className);
            clazz.getMethods();


        } catch (Throwable t) {
            if (errorListener!=null){
                errorListener.add(t);
            }
            return false;
        }

        return true;

    }
}