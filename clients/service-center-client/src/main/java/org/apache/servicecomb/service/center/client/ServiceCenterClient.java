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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.http.client.common.HttpResponse;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.apache.servicecomb.http.client.common.HttpUtils;
import org.apache.servicecomb.service.center.client.OperationEvents.UnAuthorizedOperationEvent;
import org.apache.servicecomb.service.center.client.exception.OperationException;
import org.apache.servicecomb.service.center.client.model.CreateMicroserviceInstanceRequest;
import org.apache.servicecomb.service.center.client.model.CreateMicroserviceRequest;
import org.apache.servicecomb.service.center.client.model.CreateSchemaRequest;
import org.apache.servicecomb.service.center.client.model.ErrorMessage;
import org.apache.servicecomb.service.center.client.model.FindMicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.GetSchemaListResponse;
import org.apache.servicecomb.service.center.client.model.GetSchemaResponse;
import org.apache.servicecomb.service.center.client.model.HeartbeatsRequest;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceResponse;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroserviceResponse;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.apache.servicecomb.service.center.client.model.ModifySchemasRequest;
import org.apache.servicecomb.service.center.client.model.RbacTokenRequest;
import org.apache.servicecomb.service.center.client.model.RbacTokenResponse;
import org.apache.servicecomb.service.center.client.model.RegisteredMicroserviceInstanceResponse;
import org.apache.servicecomb.service.center.client.model.RegisteredMicroserviceResponse;
import org.apache.servicecomb.service.center.client.model.SchemaInfo;
import org.apache.servicecomb.service.center.client.model.UpdatePropertiesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class ServiceCenterClient implements ServiceCenterOperation {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterClient.class);

  private final ServiceCenterRawClient httpClient;

  private EventBus eventBus;

  public ServiceCenterClient(ServiceCenterRawClient httpClient) {
    this.httpClient = httpClient;
  }

  public ServiceCenterClient setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
    return this;
  }

  public ServiceCenterClient(AddressManager addressManager,
      SSLProperties sslProperties,
      RequestAuthHeaderProvider requestAuthHeaderProvider,
      String tenantName,
      Map<String, String> extraGlobalHeaders) {
    HttpTransport httpTransport = HttpTransportFactory.createHttpTransport(sslProperties, requestAuthHeaderProvider);
    httpTransport.addHeaders(extraGlobalHeaders);

    this.httpClient = new ServiceCenterRawClient.Builder()
        .setTenantName(tenantName)
        .setAddressManager(addressManager)
        .setHttpTransport(httpTransport).build();
  }

  @Override
  public MicroserviceInstancesResponse getServiceCenterInstances() {
    try {
      HttpResponse response = httpClient.getHttpRequest("/registry/health", null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return HttpUtils.deserialize(response.getContent(), MicroserviceInstancesResponse.class);
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "get service-center instances fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "get service-center instances fails", e);
    }
  }

  @Override
  public RegisteredMicroserviceResponse registerMicroservice(Microservice microservice) {
    try {
      CreateMicroserviceRequest request = new CreateMicroserviceRequest();
      request.setService(microservice);
      HttpResponse response = httpClient
          .postHttpRequest("/registry/microservices", null, HttpUtils.serialize(request));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return HttpUtils.deserialize(response.getContent(), RegisteredMicroserviceResponse.class);
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "register service fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "register service fails", e);
    }
  }

  @Override
  public MicroservicesResponse getMicroserviceList() {
    try {
      HttpResponse response = httpClient.getHttpRequest("/registry/microservices", null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return HttpUtils.deserialize(response.getContent(), MicroservicesResponse.class);
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "get service List fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "get service List fails", e);
    }
  }

  @Override
  public RegisteredMicroserviceResponse queryServiceId(Microservice microservice) {
    try {
      URIBuilder uriBuilder = new URIBuilder("/registry/existence");
      uriBuilder.setParameter("type", "microservice");
      uriBuilder.setParameter("appId", microservice.getAppId());
      uriBuilder.setParameter("serviceName", microservice.getServiceName());
      uriBuilder.setParameter("version", microservice.getVersion());
      uriBuilder.setParameter("env", microservice.getEnvironment());

      HttpResponse response = httpClient.getHttpRequest(uriBuilder.build().toString(), null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return HttpUtils.deserialize(response.getContent(), RegisteredMicroserviceResponse.class);
      }
      sendUnAuthorizedEvent(response);
      LOGGER.info("Query serviceId fails, statusCode = " + response.getStatusCode() + "; message = " + response
          .getMessage()
          + "; content = " + response.getContent());
      return null;
    } catch (IOException e) {
      throw new OperationException(
          "query serviceId fails", e);
    } catch (URISyntaxException e) {
      throw new OperationException(
          "build url failed.", e);
    }
  }

  @Override
  public Microservice getMicroserviceByServiceId(String serviceId) {
    try {
      HttpResponse response = httpClient.getHttpRequest("/registry/microservices/" + serviceId, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        MicroserviceResponse microserviceResponse = HttpUtils
            .deserialize(response.getContent(), MicroserviceResponse.class);
        return microserviceResponse.getService();
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "get service message fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "get service message fails", e);
    }
  }

  @Override
  public RegisteredMicroserviceInstanceResponse registerMicroserviceInstance(MicroserviceInstance instance) {
    try {
      CreateMicroserviceInstanceRequest request = new CreateMicroserviceInstanceRequest();
      request.setInstance(instance);
      HttpResponse response = httpClient
          .postHttpRequest("/registry/microservices/" + instance.getServiceId() + "/instances", null,
              HttpUtils.serialize(request));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return HttpUtils.deserialize(response.getContent(), RegisteredMicroserviceInstanceResponse.class);
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "register service instance fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "register service instance fails", e);
    }
  }

  @Override
  public FindMicroserviceInstancesResponse findMicroserviceInstance(String consumerId, String appId, String serviceName,
      String versionRule,
      String revision) {
    try {
      Map<String, String> headers = new HashMap<>();
      headers.put("X-ConsumerId", consumerId);
      HttpResponse response = httpClient
          .getHttpRequest("/registry/instances?appId=" + URLEncoder.encode(appId, "UTF-8")
                  + "&serviceName=" + HttpUtils.encodeURLParam(serviceName)
                  + "&version=" + HttpUtils.encodeURLParam(versionRule)
                  + "&rev=" + HttpUtils.encodeURLParam(revision)
              , headers, null);
      FindMicroserviceInstancesResponse result = new FindMicroserviceInstancesResponse();
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        result.setModified(true);
        result.setRevision(response.getHeader("X-Resource-Revision"));
        result.setMicroserviceInstancesResponse(
            HttpUtils.deserialize(response.getContent(), MicroserviceInstancesResponse.class));
        return result;
      }
      if (response.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
        result.setModified(false);
        return result;
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "get service instances list fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "get service instances list fails", e);
    }
  }

  @Override
  public MicroserviceInstancesResponse getMicroserviceInstanceList(String serviceId) {
    try {
      HttpResponse response = httpClient
          .getHttpRequest("/registry/microservices/" + serviceId + "/instances", null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return HttpUtils.deserialize(response.getContent(), MicroserviceInstancesResponse.class);
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "get service instances list fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "get service instances list fails", e);
    }
  }

  @Override
  public MicroserviceInstance getMicroserviceInstance(String serviceId, String instanceId) {
    try {
      HttpResponse response = httpClient
          .getHttpRequest("/registry/microservices/" + serviceId + "/instances/" + instanceId, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        MicroserviceInstanceResponse instanceResponse = HttpUtils
            .deserialize(response.getContent(), MicroserviceInstanceResponse.class);
        return instanceResponse.getInstance();
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "get service instance message fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "get service instance message fails", e);
    }
  }

  @Override
  public void deleteMicroserviceInstance(String serviceId, String instanceId) {
    try {
      HttpResponse response = httpClient
          .deleteHttpRequest("/registry/microservices/" + serviceId + "/instances/" + instanceId, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("Delete service instance successfully.");
        return;
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "delete service instance fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "delete service instance fails", e);
    }
  }

  @Override
  public boolean updateMicroserviceInstanceStatus(String serviceId, String instanceId,
      MicroserviceInstanceStatus status) {
    try {
      HttpResponse response = httpClient.putHttpRequest(
          "/registry/microservices/" + serviceId + "/instances/" + instanceId + "/status?value=" + status, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return true;
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "update service instance status fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "update service instance status fails", e);
    }
  }

  @Override
  public void sendHeartBeats(HeartbeatsRequest heartbeatsRequest) {
    try {
      HttpResponse response = httpClient
          .putHttpRequest("/registry/heartbeats", null, HttpUtils.serialize(heartbeatsRequest));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return;
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "heartbeats fails, statusCode = " + response.getStatusCode() + "; message = " + response.getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "heartbeats fails ", e);
    }
  }

  @Override
  public boolean sendHeartBeat(String serviceId, String instanceId) {
    try {
      HttpResponse response = httpClient
          .putHttpRequest("/registry/microservices/" + serviceId + "/instances/" + instanceId + "/heartbeat",
              null, null);

      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return true;
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "heartbeats fails, statusCode = " + response.getStatusCode() + "; message = " + response.getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "heartbeats fails ", e);
    }
  }

  @Override
  public List<SchemaInfo> getServiceSchemasList(String serviceId, boolean withContent) {
    String url = "/registry/microservices/" + serviceId + "/schemas";
    if (withContent) {
      url = url + "?withSchema=1";
    }

    try {
      HttpResponse response = httpClient
          .getHttpRequest(url, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        GetSchemaListResponse getSchemaResponse = HttpUtils
            .deserialize(response.getContent(), GetSchemaListResponse.class);
        return getSchemaResponse.getSchemas();
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "get service schemas list fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
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
        GetSchemaResponse getSchemaResponse = HttpUtils.deserialize(response.getContent(), GetSchemaResponse.class);
        return getSchemaResponse.getSchema();
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "get service schema context fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "get service schemas context fails", e);
    }
  }

  @Override
  public boolean registerSchema(String serviceId, String schemaId, CreateSchemaRequest schema) {
    try {
      HttpResponse response = httpClient
          .putHttpRequest("/registry/microservices/" + serviceId + "/schemas/" + schemaId, null,
              HttpUtils.serialize(schema));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return true;
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "update service schema fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "update service schema fails", e);
    }
  }

  @Override
  public boolean updateServiceSchemaContext(String serviceId, SchemaInfo schemaInfo) {
    try {
      CreateSchemaRequest request = new CreateSchemaRequest();
      request.setSchema(schemaInfo.getSchema());
      request.setSummary(schemaInfo.getSummary());
      HttpResponse response = httpClient
          .putHttpRequest("/registry/microservices/" + serviceId + "/schemas/" + schemaInfo.getSchemaId(), null,
              HttpUtils.serialize(request));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return true;
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "update service schema fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "update service schema fails", e);
    }
  }

  @Override
  public boolean batchUpdateServiceSchemaContext(String serviceId, ModifySchemasRequest modifySchemasRequest) {
    try {
      HttpResponse response = httpClient
          .postHttpRequest("/registry/microservices/" + serviceId + "/schemas", null,
              HttpUtils.serialize(modifySchemasRequest));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return true;
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
          "update service schema fails, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "update service schema fails", e);
    }
  }

  private void sendUnAuthorizedEvent(HttpResponse response) {
    if (this.eventBus != null && response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
      this.eventBus.post(new UnAuthorizedOperationEvent());
    }
  }

  @Override
  public RbacTokenResponse queryToken(RbacTokenRequest request) {
    try {
      HttpResponse response = httpClient
          .postHttpRequestAbsoluteUrl("/v4/token", null,
              HttpUtils.serialize(request));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        RbacTokenResponse result = HttpUtils.deserialize(response.getContent(), RbacTokenResponse.class);
        result.setStatusCode(HttpStatus.SC_OK);
        return result;
      }
      if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
        RbacTokenResponse result = new RbacTokenResponse();
        result.setStatusCode(response.getStatusCode());
        return result;
      }
      if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED ||
          response.getStatusCode() == HttpStatus.SC_FORBIDDEN) {
        RbacTokenResponse result = new RbacTokenResponse();
        result.setStatusCode(response.getStatusCode());
        ErrorMessage errorMessage = HttpUtils.deserialize(response.getContent(), ErrorMessage.class);
        result.setErrorCode(errorMessage.getErrorCode());
        return result;
      }
      throw new OperationException(
          "query token failed, statusCode = " + response.getStatusCode() + "; message = " + response
              .getMessage()
              + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
          "query token failed", e);
    }
  }

  @Override
  public boolean updateMicroserviceProperties(String serviceId, Map<String, String> serviceProperties) {
    try {
      UpdatePropertiesRequest request = new UpdatePropertiesRequest();
      request.setProperties(serviceProperties);
      HttpResponse response = httpClient.putHttpRequest(
              "/registry/microservices/" + serviceId + "/properties", null, HttpUtils.serialize(request));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return true;
      }
      sendUnAuthorizedEvent(response);
      throw new OperationException(
              "update service instance status fails, statusCode = " + response.getStatusCode() + "; message = " + response
                      .getMessage()
                      + "; content = " + response.getContent());
    } catch (IOException e) {
      throw new OperationException(
              "update service instance status fails", e);
    }
  }
}
