package org.apache.axis.deployment.test;

import org.apache.axis.deployment.scheduler.Scheduler;
import org.apache.axis.deployment.scheduler.SchedulerTask;
import org.apache.axis.deployment.scheduler.DeploymentIterator;
import org.apache.axis.deployment.DeployCons;
import org.apache.axis.deployment.DeployMangerImpl;
import org.apache.axis.deployment.phaserule.PhaseException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Oct 5, 2004
 *         9:43:39 AM
 *
 */
public class HotDeploymentTest implements DeployCons{
    private final Scheduler scheduler = new Scheduler();
    private final SimpleDateFormat dateFormat =  new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
    private final int hourOfDay, minute, second;

    public HotDeploymentTest() {
        Date date = new Date();
        this.hourOfDay = date.getHours();
        this.minute = date.getMinutes();
        this.second = date.getSeconds();
    }

    public void start() {
       scheduler.schedule(new SchedulerTask(),new DeploymentIterator(hourOfDay, minute, second));
    }

    private String resolveFileName(String fileName){
        fileName = fileName.trim();
        int namelen = fileName.length();
        char fws = '/';   // forward seperator
        char bws = '\\'; // backword seperator
        int index = 0 ; // index of the seperator

        /**
         * following if condition is needed bcos of the os
         */
        index = fileName.lastIndexOf(fws, namelen -1);
        if(index == -1){
            index = fileName.lastIndexOf(bws, namelen -1);
        }
        fileName = fileName.substring(index+1,namelen);
        // fileName.
        return fileName;
    }


    public static void main(String[] args) {
        HotDeploymentTest alarmClock = new HotDeploymentTest();
       /* String fileanme ="D:\\Axis 2.0\\projects\\Deployement\\lib\\junit1.jar";
        try {
            FileInputStream fileInputStream = new FileInputStream(fileanme) ;
            DeployMangerImpl deployManger = new DeployMangerImpl();
            deployManger.deployWS(fileInputStream,fileanme);
            deployManger.undeployeWS("lxpp.jar");
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch(Exception k){
            k.printStackTrace();
        }  */
        //
        //String fileanme ="/192.168.101.12/shared/mp3/junit.jar";
     //   String fileanme ="http://java.sun.com/developer/JDCTechTips/2004/tt0727.html";
     //   String name = alarmClock.resolveFileName(fileanme);
     //   System.out.println("File B4Name  " + fileanme);
     //   System.out.println("File Name  " + FOLDE_NAME + "/" + name);
        alarmClock.start();
    }
}


