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
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.servicecomb.service.center.client.exception.OperationException;
import org.apache.servicecomb.service.center.client.http.HttpResponse;
import org.apache.servicecomb.service.center.client.http.HttpTransport;
import org.apache.servicecomb.service.center.client.http.HttpTransportFactory;
import org.apache.servicecomb.service.center.client.http.TLSConfig;
import org.apache.servicecomb.service.center.client.http.TLSHttpsTransport;
import org.apache.servicecomb.service.center.client.model.GetSchemaResponse;
import org.apache.servicecomb.service.center.client.model.HeartbeatsRequest;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
  public ServiceCenterClient(TLSConfig tlsConfig) {
    this(tlsConfig, null);
  }

  /**
   * Add extraGlobalHeaders to http request
   * @param extraGlobalHeaders
   */
  public ServiceCenterClient(Map<String, String> extraGlobalHeaders) {
    this(null, 0, null, null, null, extraGlobalHeaders);
  }

  /**
   * Add TLS config and extraGlobalHeaders
   * @param tlsConfig
   * @param extraGlobalHeaders
   */
  public ServiceCenterClient(TLSConfig tlsConfig, Map<String, String> extraGlobalHeaders) {
    this(null, 0, null, null, tlsConfig, extraGlobalHeaders);
  }

  /**
   * Customized host,port,
   * @param host
   * @param port
   */
  public ServiceCenterClient(String host, int port) {
    this(host, port, null, null, null, null);
  }

  /**
   * Customized host, port, projectName, tenantName, TLSConf, headers and any one parameter can be null.
   * @param host
   * @param port
   * @param projectName
   * @param tenantName
   * @param tlsConfig
   * @param extraGlobalHeaders
   */
  public ServiceCenterClient(String host, int port, String projectName, String tenantName, TLSConfig tlsConfig,
      Map<String, String> extraGlobalHeaders) {
    HttpTransport httpTransport = HttpTransportFactory.getDefaultHttpTransport();
    if (tlsConfig != null) {
      httpTransport = new TLSHttpsTransport(tlsConfig);
    }
    httpTransport.addHeaders(extraGlobalHeaders);

    this.httpClient = new ServiceCenterRawClient.Builder()
        .setHost(host)
        .setPort(port)
        .setProjectName(projectName)
        .setTenantName(tenantName)
        .setHttpTransport(httpTransport).build();
  }

  public ServiceCenterClient(ServiceCenterRawClient serviceCenterRawClient) {
    this.httpClient = serviceCenterRawClient;
  }

  /**
   * Get service-center instances message
   *
   * @return MicroserviceInstancesResponse
   * @throws OperationException
   */
  public MicroserviceInstancesResponse getServiceCenterInstances() {
    try {
      HttpResponse response = httpClient.getHttpRequest("/registry/health", null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.getContent(), MicroserviceInstancesResponse.class);
      } else {
        throw new OperationException(
            "get service-center instances fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service-center instances fails", e);
    }
  }

  /**
   * Register microservice to service-center
   *
   * @param microservice
   * @return serviceId
   * @throws OperationException
   */
  public String registerMicroservice(Microservice microservice) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
      HttpResponse response = httpClient
          .postHttpRequest("/registry/microservices", null, mapper.writeValueAsString(microservice));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return response.getContent();
      } else {
        throw new OperationException(
            "register service fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "register service fails", e);
    }
  }

  /**
   * find all registerd microservice of service-center
   *
   * @return MicroserviceResponse
   * @throws OperationException
   */
  public MicroservicesResponse getMicroserviceList() {
    try {
      HttpResponse response = httpClient.getHttpRequest("/registry/microservices", null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(response.getContent(), MicroservicesResponse.class);
      } else {
        throw new OperationException(
            "get service List fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service List fails", e);
    }
  }

  /**
   * query serviceId, temporary only supports Microservice type
   *
   * @param microservice
   * @return serviceId
   * @throws OperationException
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
        return response.getContent();
      } else {
        throw new OperationException(
            "query serviceId fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "query serviceId fails", e);
    } catch (URISyntaxException e) {
      throw new OperationException(
          "build url failed.", e);
    }
  }

  /**
   * Get one microservice message of service-center
   *
   * @param serviceId
   * @return Microservice
   * @throws OperationException
   */
  @SuppressWarnings("unchecked")
  public Microservice getMicroserviceByServiceId(String serviceId) {
    try {
      HttpResponse response = httpClient.getHttpRequest("/registry/microservices/" + serviceId, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode jsonNode = mapper.readTree(response.getContent());
        return mapper.readValue(jsonNode.get("service").toString(), Microservice.class);
      } else {
        throw new OperationException(
            "get service message fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service message fails", e);
    }
  }

  /**
   * Register microservice instances to service-center
   *
   * @param instance
   * @param serviceId
   * @return instanceId
   * @throws OperationException
   */
  public String registerMicroserviceInstance(MicroserviceInstance instance, String serviceId) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
      HttpResponse response = httpClient.postHttpRequest("/registry/microservices/" + serviceId + "/instances", null,
          mapper.writeValueAsString(instance));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return response.getContent();
      } else {
        throw new OperationException(
            "register service instance fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "register service instance fails", e);
    }
  }

  /**
   * Find microservice instances of service-center
   *
   * @param serviceId
   * @return MicroserviceInstancesResponse
   * @throws OperationException
   */
  public MicroserviceInstancesResponse getMicroserviceInstanceList(String serviceId) {
    try {
      HttpResponse response = httpClient
          .getHttpRequest("/registry/microservices/" + serviceId + "/instances", null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.getContent(), MicroserviceInstancesResponse.class);
      } else {
        throw new OperationException(
            "get service instances list fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service instances list fails", e);
    }
  }

  /**
   * Get microservice instance message of service-center
   *
   * @param serviceId
   * @param instanceId
   * @return MicroserviceInstance
   * @throws OperationException
   */
  @SuppressWarnings("unchecked")
  public MicroserviceInstance getMicroserviceInstance(String serviceId, String instanceId) {
    try {
      HttpResponse response = httpClient
          .getHttpRequest("/registry/microservices/" + serviceId + "/instances/" + instanceId, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getContent());
        return mapper.readValue(jsonNode.get("instance").toString(), MicroserviceInstance.class);
      } else {
        throw new OperationException(
            "get service instance message fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service instance message fails", e);
    }
  }

  /**
   * Delete a microservice instance
   *
   * @param serviceId
   * @param instanceId
   * @return
   * @throws OperationException
   */
  public void deleteMicroserviceInstance(String serviceId, String instanceId) {
    try {
      HttpResponse response = httpClient
          .deleteHttpRequest("/registry/microservices/" + serviceId + "/instances/" + instanceId, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("DELETE SERVICE INSTANCE OK");
      } else {
        throw new OperationException(
            "delete service instance fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "delete service instance fails", e);
    }
  }

  /**
   * Update status of microservice Instance
   *
   * @param serviceId
   * @param instanceId
   * @param status
   * @return true
   * @throws OperationException
   */
  public boolean updateMicroserviceInstanceStatus(String serviceId, String instanceId,
      MicroserviceInstanceStatus status) {
    try {
      HttpResponse response = httpClient.putHttpRequest(
          "/registry/microservices/" + serviceId + "/instances/" + instanceId + "/status?value=" + status, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("UPDATE STATUS OK");
        return true;
      } else {
        throw new OperationException(
            "update service instance status fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "update service instance status fails", e);
    }
  }

  /**
   * Batch send heartbeats to service-center
   *
   * @param heartbeatsRequest
   * @return
   * @throws OperationException
   */
  public void sendHeartBeats(HeartbeatsRequest heartbeatsRequest) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      HttpResponse response = httpClient
          .putHttpRequest("/registry/heartbeats", null, mapper.writeValueAsString(heartbeatsRequest));

      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("HEARTBEATS SUCCESS");
      } else {
        throw new OperationException(
            "heartbeats fails, statusCode = " + response.getStatusCode() + "; message = " + response.getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "heartbeats fails ", e);
    }
  }

  /**
   * Get schemas list of service
   *
   * @param serviceId
   * @return
   * @throws OperationException
   */
  public List<GetSchemaResponse> getServiceSchemasList(String serviceId) {
    try {
      HttpResponse response = httpClient
          .getHttpRequest("/registry/microservices/" + serviceId + "/schemas", null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getContent());
        return mapper.readValue(jsonNode.get("schemas").toString(), new TypeReference<List<GetSchemaResponse>>() {
        });
      } else {
        throw new OperationException(
            "get service schemas list fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service schemas list fails", e);
    }
  }

  /**
   * Get one schema context of service
   *
   * @param serviceId
   * @param schemaId
   * @return
   * @throws OperationException
   */
  public String getServiceSchemaContext(String serviceId, String schemaId) {
    try {
      HttpResponse response = httpClient
          .getHttpRequest("/registry/microservices/" + serviceId + "/schemas/" + schemaId, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getContent());
        return jsonNode.get("schema").textValue();
      } else {
        throw new OperationException(
            "get service schema context fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service schemas context fails", e);
    }
  }

  /**
   * update schema context of service
   *
   * @param serviceId
   * @param schemaId
   * @param schemaResponse
   * @return
   * @throws OperationException
   */
  public Boolean updateServiceSchemaContext(String serviceId, String schemaId, GetSchemaResponse schemaResponse) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      HttpResponse response = httpClient
          .putHttpRequest("/registry/microservices/" + serviceId + "/schemas/" + schemaId, null,
              mapper.writeValueAsString(schemaResponse));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("UPDATE SCHEMA OK");
        return true;
      } else {
        throw new OperationException(
            "update service schema fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "update service schema fails", e);
    }
  }
}
