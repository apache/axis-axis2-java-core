package org.apache.axis.interop.util;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Feb 22, 2005
 * Time: 5:49:58 PM
 */
public class InteropTO {
    private String URL;
    private String SOAPAction;
    private String StringValue;
    private String [] arraValue;
    private String request;
    private String response;

    private String structString;
    private int structint;
    private float structfloat;

    private int type;


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getSOAPAction() {
        return SOAPAction;
    }

    public void setSOAPAction(String SOAPAction) {
        this.SOAPAction = SOAPAction;
    }

    public String getStringValue() {
        return StringValue;
    }

    public void setStringValue(String stringValue) {
        this.StringValue = stringValue;
    }

    public String[] getArraValue() {
        return arraValue;
    }

    public void setArraValue(String[] arraValue) {
        this.arraValue = arraValue;
    }

    public String getStructString() {
        return structString;
    }

    public void setStructString(String structString) {
        this.structString = structString;
    }

    public int getStructint() {
        return structint;
    }

    public void setStructint(int structint) {
        this.structint = structint;
    }

    public float getStructfloat() {
        return structfloat;
    }

    public void setStructfloat(float structfloat) {
        this.structfloat = structfloat;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void printMe(){
        System.out.println("URL = " + URL);
        System.out.println("StringValue = " + StringValue);
        System.out.println("type = " + type);
        System.out.println("SOAPAction = " + SOAPAction);
    }
}
