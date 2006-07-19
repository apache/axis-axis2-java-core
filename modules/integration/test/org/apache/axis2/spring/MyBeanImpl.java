package org.apache.axis2.spring;

/** Spring wired implementation */
public class MyBeanImpl implements MyBean {

    String str = null;
    // spring 'injects' this value 
    public void setVal(String s) { 
        str = s;
    }
    // web service gets this value
    public String emerge() {
        return str;
    }
}
