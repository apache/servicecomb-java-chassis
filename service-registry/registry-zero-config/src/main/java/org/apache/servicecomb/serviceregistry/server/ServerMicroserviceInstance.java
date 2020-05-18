/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.serviceregistry.server;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ServerMicroserviceInstance {
    private String appId;

    private String serviceName;

    private String version;

    private String instanceId;

    private String serviceId;

    private List<String> endpoints = new ArrayList<>();

    private List<String> schemas = new ArrayList<>();

    private String hostName;

    private String status;

    private Instant lastHeartbeatTimeStamp;

    public ServerMicroserviceInstance(){}

    public Instant getLastHeartbeatTimeStamp() {
        return lastHeartbeatTimeStamp;
    }

    public void setLastHeartbeatTimeStamp(Instant lastHeartbeatTimeStamp) {
        this.lastHeartbeatTimeStamp = lastHeartbeatTimeStamp;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    @Override
    public String toString() {
        return "ServerMicroserviceInstance{" +
                "appId='" + appId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", version='" + version + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", endpoints=" + endpoints +
                ", schemas=" + schemas +
                ", hostName='" + hostName + '\'' +
                ", status='" + status + '\'' +
                ", lastHeartbeatTimeStamp=" + lastHeartbeatTimeStamp +
                '}';
    }
}
