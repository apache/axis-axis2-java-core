package org.apache.idaeplugin.bean;
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
*
*
*/

/**
 * Author: Deepal Jayasinghe
 * Date: Sep 21, 2005
 * Time: 11:36:02 PM
 */
public class OprationObj {

    private String OpName ;
    private String ReturnVale;
    private Integer paramters;
    private Boolean select;

    public OprationObj(String opName, String returnVale, Integer paramters, Boolean select) {
        OpName = opName;
        ReturnVale = returnVale;
        this.paramters = paramters;
        this.select = select;
    }


    public String getOpName() {
        return OpName;
    }

    public void setOpName(String opName) {
        OpName = opName;
    }

    public String getReturnVale() {
        return ReturnVale;
    }

    public void setReturnVale(String returnVale) {
        ReturnVale = returnVale;
    }

    public Integer getParamters() {
        return paramters;
    }

    public void setParamters(Integer paramters) {
        this.paramters = paramters;
    }

    public Boolean getSelect() {
        return select;
    }

    public void setSelect(Boolean select) {
        this.select = select;
    }

    public void printMe(){
        System.out.println("======== Row =============");
        System.out.println("OpName = " + OpName);
        System.out.println("paramters = " + paramters);
        System.out.println("ReturnVale = " + ReturnVale);
        System.out.println("select = " + select);
        System.out.println("==========================");
    }

}
