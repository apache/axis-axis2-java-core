package org.apache.axis.util;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Ajith
 * Date: Feb 17, 2005
 * Time: 5:58:09 PM
 */
public class ServiceItemBean {
    private String serviceName;
    private ArrayList operationsList;

    public ServiceItemBean() {
        operationsList = new ArrayList();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void addOperation(OperationItemBean operation){
        this.operationsList.add(operation);
    }

    public OperationItemBean getOperation(int index){
         return (OperationItemBean)operationsList.get(index);
    }

    public int OperationCount(){
        return operationsList.size();
    }
}
