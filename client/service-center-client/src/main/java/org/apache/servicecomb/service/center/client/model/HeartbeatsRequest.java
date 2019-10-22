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

package org.apache.servicecomb.service.center.client.model;

import java.util.ArrayList;
import java.util.List;

public class HeartbeatsRequest {
    private List<InstancesRequest> Instances;

    public HeartbeatsRequest(String serviceId, String instanceId) {
        List<InstancesRequest> instances = new ArrayList<InstancesRequest>();
        instances.add(new InstancesRequest(serviceId, instanceId));
        this.Instances = instances;
    }

    public List<InstancesRequest> getInstances() {
        return Instances;
    }

    public void setInstances(List<InstancesRequest> instances) {
        this.Instances = instances;
    }

    public void addInstances(InstancesRequest instancesRequest) {
        if (this.Instances != null) {
            this.Instances.add(instancesRequest);
        } else {
            this.Instances = new ArrayList<InstancesRequest>();
            this.Instances.add(instancesRequest);
        }
    }
}
