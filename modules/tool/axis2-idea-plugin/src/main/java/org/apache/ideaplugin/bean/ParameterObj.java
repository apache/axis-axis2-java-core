package org.apache.ideaplugin.bean;

public class ParameterObj {

    private String  paraName;
    private String paraValue;

    public ParameterObj(String name,String value){
        this.paraName=name;
        this.paraValue=value;
    }

    public void setName(String name){
        this.paraName=name;
    }

    public void setValue(String value){
        this.paraValue=value;
    }

    public String  getName(){
        return this.paraName;
    }

    public String  getValue(){
        return this.paraValue;
    }

}
