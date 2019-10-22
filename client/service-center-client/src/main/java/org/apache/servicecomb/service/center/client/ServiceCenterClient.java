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

package org.apache.servicecomb.service.center.client;

import java.io.IOException;
import java.util.Map;

import javax.management.OperationsException;

import org.apache.http.HttpStatus;
import org.apache.servicecomb.service.center.client.http.HttpResponse;
import org.apache.servicecomb.service.center.client.model.HeartbeatsRequest;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * Created by   on 2019/10/16.
 */
public class ServiceCenterClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterClient.class);

    private ServiceCenterRawClient httpClient = new ServiceCenterRawClient();

    public ServiceCenterClient() {

    }

    public ServiceCenterClient(ServiceCenterRawClient serviceCenterRawClient) {
        this.httpClient = serviceCenterRawClient;
    }

    /**
     * Get service-center instances message
     *
     * @return
     */
    public MicroserviceInstancesResponse getServiceCenterInstances() {
        try {
            HttpResponse response = httpClient.getHttpRequest("/registry/health", null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("getServiceCenterInstances result = " + response.getContent());
                return JSON.parseObject(response.getContent(), MicroserviceInstancesResponse.class);
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (OperationsException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Register microservice to service-center
     *
     * @param microservice
     * @return
     * @throws IOException
     */
    public String registerMicroservice(Microservice microservice) {
        try {
            JSONObject body = new JSONObject();
            body.put("service", microservice);
            HttpResponse response = httpClient
                    .postHttpRequest("/registry/microservices", null, body.toString(SerializerFeature.WriteMapNullValue));
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("registerService result = " + response.getContent());
                return response.getContent();
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (OperationsException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * find all registerd microservice of service-center
     *
     * @return
     */
    public MicroservicesResponse getMicroservices() {
        try {
            HttpResponse response = httpClient.getHttpRequest("/registry/microservices", null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("getService result = " + response.getContent());
                return JSON.parseObject(response.getContent(), MicroservicesResponse.class);
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (OperationsException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get one microservice message of service-center
     *
     * @param serviceId
     * @return
     */
    @SuppressWarnings("unchecked")
    public Microservice getMicroserviceByServiceId(String serviceId) {
        try {
            HttpResponse response = httpClient.getHttpRequest("/registry/microservices/" + serviceId, null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("GetServiceMessage result = " + response.getContent());
                Map<String, String> maps = (Map<String, String>) JSON.parse(response.getContent());
                return JSON.parseObject(JSONObject.toJSONString(maps.get("service")), Microservice.class);
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (IOException | OperationsException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Register microservice instances to service-center
     *
     * @param instance
     * @param serviceId
     * @return
     * @throws IOException
     */
    public String registerMicroserviceInstance(MicroserviceInstance instance, String serviceId) {
        try {
            JSONObject body = new JSONObject();
            body.put("instance", instance);
            HttpResponse response = httpClient.postHttpRequest("/registry/microservices/" + serviceId + "/instances", null,
                    body.toString(SerializerFeature.WriteMapNullValue));

            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("registerServieInstance result = " + response.getContent());
                return response.getContent();
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (OperationsException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find microservice instances of service-center
     *
     * @param serviceId
     * @return
     */
    public MicroserviceInstancesResponse getMicroserviceInstancesByServiceId(String serviceId) {
        try {
            HttpResponse response = httpClient
                    .getHttpRequest("/registry/microservices/" + serviceId + "/instances", null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("getServiceInstance result = " + response.getContent());
                return JSON.parseObject(response.getContent(), MicroserviceInstancesResponse.class);
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (IOException | OperationsException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get microservice instance message of service-center
     *
     * @param serviceId
     * @param instanceId
     * @return
     */
    @SuppressWarnings("unchecked")
    public MicroserviceInstance getMicroserviceInstanceByServiceIdAndInstanceId(String serviceId, String instanceId) {
        try {
            HttpResponse response = httpClient
                    .getHttpRequest("/registry/microservices/" + serviceId + "/instances/" + instanceId, null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("GetServieInstanceMessage result = " + response.getContent());
                if (response.getContent() != null) {
                    Map<String, String> maps = (Map<String, String>) JSON.parse(response.getContent());
                    return JSON.parseObject(JSONObject.toJSONString(maps.get("instance")), MicroserviceInstance.class);
                }
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (IOException | OperationsException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete a microservice instance
     *
     * @param serviceId
     * @param instanceId
     * @return
     */
    public String deleteMicroserviceInstance(String serviceId, String instanceId) {
        try {
            HttpResponse response = httpClient
                    .deleteHttpRequest("/registry/microservices/" + serviceId + "/instances/" + instanceId, null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("DELETE SERVICE INSTANCE OK");
                return response.getContent();
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (IOException | OperationsException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update status of microservice Instance
     *
     * @param serviceId
     * @param instanceId
     * @param status
     * @return
     */
    public String updateMicroservicesInstanceStatus(String serviceId, String instanceId,
                                                    MicroserviceInstanceStatus status) {
        try {
            HttpResponse response = httpClient.putHttpRequest(
                    "/registry/microservices/" + serviceId + "/instances/" + instanceId + "/status?value=" + status, null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("UPDATE STATUS OK");
                return response.getContent();
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (OperationsException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Batch send heartbeats to service-center
     *
     * @param heartbeatsRequest
     * @return
     */
    public String sendHeartBeats(HeartbeatsRequest heartbeatsRequest) {
        try {
            HttpResponse response = httpClient
                    .putHttpRequest("/registry/heartbeats", null, JSONObject.toJSONString(heartbeatsRequest));

            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("SEND HEARTBEATS OK");
                return response.getContent();
            } else {
                throw new OperationsException(response.getStatusCode() + response.getMessage() + response.getContent());
            }
        } catch (OperationsException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
