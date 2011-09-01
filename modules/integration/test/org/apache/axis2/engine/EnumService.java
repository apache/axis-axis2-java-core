/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.engine;

public class EnumService {

    public EnumService() {
    }

    public Day testDay(Day day){
        System.out.println(day.getClass().getName());
        return day;
    }

    public Event enumPojo(Event newEvent){
        return newEvent;
    }

    public Status testMultiEnumParameters(Status status1 , Day day1 , Status status2 , Day day){
        return status1;
    }

    public enum Day{
        MONDAY , TUESDAY , WEDNESDAY, THURSDAY , FRIDAY , SATURDAY , SUNDAY;
    }



    public enum Status{
        START(0,"start"), ACTIVE(1,"active") , STOP(2, "stop");

        private final int val;
        private final String desc;
        Status(int val , String desc){
            this.val = val;
            this.desc =  desc;
        }

        public int value(){
            return this.val;
        }

        public String description(){
            return this.desc;
        }
    }
}
