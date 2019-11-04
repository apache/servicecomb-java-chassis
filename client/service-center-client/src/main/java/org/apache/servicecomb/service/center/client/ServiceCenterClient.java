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
import java.net.URISyntaxException;
import java.util.Map;

import javax.management.OperationsException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.servicecomb.service.center.client.http.*;
import org.apache.servicecomb.service.center.client.model.HeartbeatsRequest;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by   on 2019/10/16.
 */
public class ServiceCenterClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterClient.class);

    private ServiceCenterRawClient httpClient;

    /**
     * Use default config parameter
     */
    public ServiceCenterClient() {
        this(new ServiceCenterRawClient());
    }

    /**
     * Add TLS config of client
     * @param tlsConfig
     */
    public ServiceCenterClient(TLSConfig tlsConfig){
        this(tlsConfig,null);
    }

    /**
     * Add extraGlobalHeaders to http request
     * @param extraGlobalHeaders
     */
    public ServiceCenterClient(Map<String, String> extraGlobalHeaders) {
        this(null,0,null,null,extraGlobalHeaders);
    }

    /**
     * Add TLS config and extraGlobalHeaders
     * @param tlsConfig
     * @param extraGlobalHeaders
     */
    public ServiceCenterClient(TLSConfig tlsConfig,Map<String, String> extraGlobalHeaders){
        this(null,0,null,tlsConfig,extraGlobalHeaders);
    }

    /**
     * Customized host,port,
     * @param host
     * @param port
     */
    public ServiceCenterClient(String host, int port) {
        this(host, port, null,null, null);
    }

    /**
     * Customized host,port,domainName,TLSConf, headers and any one parameter can be null.
     * @param host
     * @param port
     * @param domainName
     * @param tlsConfig
     * @param extraGlobalHeaders
     */
    public ServiceCenterClient(String host, int port, String domainName, TLSConfig tlsConfig,  Map<String, String> extraGlobalHeaders) {
        HttpTransport httpTransport = HttpTransportFactory.getDefaultHttpTransport();
        if(tlsConfig!=null){
            httpTransport = new TLSHttpsTransport(tlsConfig);
        }
        httpTransport.addHeaders(extraGlobalHeaders);
        this.httpClient = new ServiceCenterRawClient.Builder().setHost(host).setPort(port).setDomainName(domainName).setHttpTransport(httpTransport).build();
    }

    public ServiceCenterClient(ServiceCenterRawClient serviceCenterRawClient) {
        this.httpClient = serviceCenterRawClient;
    }

    /**
     * Get service-center instances message
     *
     * @return MicroserviceInstancesResponse; when error happens,return null
     */
    public MicroserviceInstancesResponse getServiceCenterInstances() {
        try {
            HttpResponse response = httpClient.getHttpRequest("/registry/health", null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("getServiceCenterInstances result = " + response.getContent());
//                return JSON.parseObject(response.getContent(), MicroserviceInstancesResponse.class);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.getContent(),MicroserviceInstancesResponse.class);
            } else {
                LOGGER.error("get service-center instances fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("get service-center instances fails",e);
        }
        return null;
    }

    /**
     * Register microservice to service-center
     *
     * @param microservice
     * @return serviceId ; when error happens,return null
     */
    public String registerMicroservice(Microservice microservice) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            HttpResponse response = httpClient
                    .postHttpRequest("/registry/microservices", null, mapper.writeValueAsString(microservice));
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("registerService result = " + response.getContent());
                return response.getContent();
            } else {
                LOGGER.error("register service fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("register service fails",e);
        }
        return null;
    }

    /**
     * find all registerd microservice of service-center
     *
     * @return MicroserviceResponse ; when error happens,return null
     */
    public MicroservicesResponse getMicroserviceList() {
        try {
            HttpResponse response = httpClient.getHttpRequest("/registry/microservices", null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("getService result = " + response.getContent());
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return mapper.readValue(response.getContent(), MicroservicesResponse.class);
            } else {
                LOGGER.error("get service List fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("get service List fails",e);
        }
        return null;
    }

    /**
     * query serviceId
     * @param microservice
     * @return serviceId ; when error happens,return null
     */
    public String queryServiceId(Microservice microservice) {
        try {
            URIBuilder uriBuilder = new URIBuilder("/registry/existence");
            uriBuilder.setParameter("type", "microservice");
            uriBuilder.setParameter("appId", microservice.getAppId());
            uriBuilder.setParameter("serviceName", microservice.getServiceName());
            uriBuilder.setParameter("version", microservice.getVersion());

            HttpResponse response = httpClient.getHttpRequest(uriBuilder.build().toString(), null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("GetServiceId result = " + response.getContent());
                return response.getContent();
            } else {
                LOGGER.error("query serviceId fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("query serviceId fails",e);
        }
        return null;
    }

    /**
     * Get one microservice message of service-center
     *
     * @param serviceId
     * @return Microservice ; when error happens,return null
     */
    @SuppressWarnings("unchecked")
    public Microservice getMicroserviceByServiceId(String serviceId) {
        try {
            HttpResponse response = httpClient.getHttpRequest("/registry/microservices/" + serviceId, null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("GetServiceMessage result = " + response.getContent());
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                JsonNode jsonNode = mapper.readTree(response.getContent());
                return mapper.readValue(jsonNode.get("service").toString(),Microservice.class);
            } else {
                LOGGER.error("get service message fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("get service message fails",e);
        }
        return null;
    }

    /**
     * Register microservice instances to service-center
     *
     * @param instance
     * @param serviceId
     * @return service instanceId ; when error happens,return null
     */
    public String registerMicroserviceInstance(MicroserviceInstance instance, String serviceId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            HttpResponse response = httpClient.postHttpRequest("/registry/microservices/" + serviceId + "/instances", null,
                    mapper.writeValueAsString(instance));

            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("register ServiceInstance result = " + response.getContent());
                return response.getContent();
            } else {
                LOGGER.error("register service instance fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("register service instance fails",e);
        }
        return null;
    }

    /**
     * Find microservice instances of service-center
     *
     * @param serviceId
     * @return MicroserviceInstancesResponse ; when error happens,return null
     */
    public MicroserviceInstancesResponse getMicroserviceInstanceList(String serviceId) {
        try {
            HttpResponse response = httpClient
                    .getHttpRequest("/registry/microservices/" + serviceId + "/instances", null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("getServiceInstance result = " + response.getContent());
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.getContent(),MicroserviceInstancesResponse.class);
            } else {
                LOGGER.error("get service instances list fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("get service instance list fails",e);
        }
        return null;
    }

    /**
     * Get microservice instance message of service-center
     *
     * @param serviceId
     * @param instanceId
     * @return MicroserviceInstance; when error happens,return null
     */
    @SuppressWarnings("unchecked")
    public MicroserviceInstance getMicroserviceInstance(String serviceId, String instanceId) {
        try {
            HttpResponse response = httpClient
                    .getHttpRequest("/registry/microservices/" + serviceId + "/instances/" + instanceId, null, null);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("GetServieInstanceMessage result = " + response.getContent());
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(response.getContent());
                return mapper.readValue(jsonNode.get("instance").toString(),MicroserviceInstance.class);
            }  else {
                LOGGER.error("get service instance message fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("get service instance message fails",e);
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
            }  else {
                LOGGER.error("delete service instance fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("delete service instance fails",e);
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
                LOGGER.error("update service instance status fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("update service instance status fails",e);
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
            ObjectMapper mapper = new ObjectMapper();
            HttpResponse response = httpClient
                    .putHttpRequest("/registry/heartbeats", null, mapper.writeValueAsString(heartbeatsRequest));

            if (response.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("SEND HEARTBEATS OK");
                return response.getContent();
            } else {
                LOGGER.error("send heartbeats fails, responseStatusCode={}, responseMessage={}, responseContent{}",response.getStatusCode(), response.getMessage(), response.getContent());
            }
        } catch (IOException e) {
            LOGGER.error("send heartbeats fails",e);
        }
        return null;
    }
}
