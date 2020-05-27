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

package org.apache.servicecomb.serviceregistry.client.http;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.api.registry.ServiceCenterInfo;
import org.apache.servicecomb.serviceregistry.api.request.CreateSchemaRequest;
import org.apache.servicecomb.serviceregistry.api.request.CreateServiceRequest;
import org.apache.servicecomb.serviceregistry.api.request.RegisterInstanceRequest;
import org.apache.servicecomb.serviceregistry.api.request.UpdatePropertiesRequest;
import org.apache.servicecomb.serviceregistry.api.response.CreateServiceResponse;
import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetAllServicesResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetExistenceResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetInstancesResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemasResponse;
import org.apache.servicecomb.serviceregistry.api.response.GetServiceResponse;
import org.apache.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import org.apache.servicecomb.registry.api.event.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceResponse;
import org.apache.servicecomb.serviceregistry.api.response.RegisterInstanceResponse;
import org.apache.servicecomb.serviceregistry.client.ClientException;
import org.apache.servicecomb.serviceregistry.client.IpPortManager;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.task.HeartbeatResult;
import org.apache.servicecomb.serviceregistry.task.MicroserviceInstanceHeartbeatTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.eventbus.Subscribe;

import io.netty.handler.codec.http.HttpStatusClass;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;

public final class ServiceRegistryClientImpl implements ServiceRegistryClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryClientImpl.class);

  private static final String ERROR_CODE = "errorCode";

  private static final String ERR_SERVICE_NOT_EXISTS = "400012";

  private static final String ERR_SCHEMA_NOT_EXISTS = "400016";

  private IpPortManager ipPortManager;

  // key是本进程的微服务id和服务管理中心的id
  // extract this, ServiceRegistryClient is better to be no status.
  private Map<String, Boolean> watchServices = new ConcurrentHashMap<>();

  private RestClientUtil restClientUtil;

  private WebsocketClientUtil websocketClientUtil;

  public ServiceRegistryClientImpl(ServiceRegistryConfig serviceRegistryConfig) {
    this.ipPortManager = new IpPortManager(serviceRegistryConfig);
    this.restClientUtil = new RestClientUtil(serviceRegistryConfig);
    this.websocketClientUtil = new WebsocketClientUtil(serviceRegistryConfig);
  }

  private LoadingCache<String, Map<String, String>> schemaCache = CacheBuilder.newBuilder()
      .expireAfterAccess(60, TimeUnit.SECONDS).build(new CacheLoader<String, Map<String, String>>() {
        public Map<String, String> load(String key) {
          Holder<List<GetSchemaResponse>> result = getSchemas(key, true, true);
          Map<String, String> schemas = new HashMap<>();
          if (result.getStatusCode() == Status.OK.getStatusCode()) {
            result.value.stream().forEach(r -> schemas.put(r.getSchemaId(), r.getSchema()));
          }
          return schemas;
        }
      });

  @Override
  public void init() {
  }

  private void retry(RequestContext requestContext, Handler<RestResponse> responseHandler) {
    LOGGER.warn("invoke service [{}] failed, retry.", requestContext.getUri());
    requestContext.setIpPort(ipPortManager.getNextAvailableAddress(requestContext.getIpPort()));
    requestContext.incrementRetryTimes();
    restClientUtil.httpDo(requestContext, responseHandler);
  }

  @VisibleForTesting
  @SuppressWarnings("unchecked")
  protected <T> Handler<RestResponse> syncHandler(CountDownLatch countDownLatch, Class<T> cls,
      Holder<T> holder) {
    return restResponse -> {
      RequestContext requestContext = restResponse.getRequestContext();
      HttpClientResponse response = restResponse.getResponse();
      if (response == null) {
        // 请求失败，触发请求SC的其他实例
        if (requestContext.getRetryTimes() <= ipPortManager.getMaxRetryTimes()) {
          retry(requestContext, syncHandler(countDownLatch, cls, holder));
        } else {
          countDownLatch.countDown();
        }
        return;
      }
      holder.setStatusCode(response.statusCode());
      response.exceptionHandler(e -> {
        LOGGER.error("error in processing response.", e);
        countDownLatch.countDown();
      });
      response.bodyHandler(
          bodyBuffer -> {
            if (cls.getName().equals(HttpClientResponse.class.getName())) {
              holder.value = (T) response;
              countDownLatch.countDown();
              return;
            }
            if (cls.equals(String.class)) {
              holder.setValue((T) bodyBuffer.toString());
              countDownLatch.countDown();
              return;
            }

            // no need to generate warn log when schema or service not exist
            if (HttpStatusClass.CLIENT_ERROR.equals(HttpStatusClass.valueOf(response.statusCode()))) {
              try {
                Map<String, String> bufferMap = JsonUtils.readValue(bodyBuffer.getBytes(), Map.class);
                if (bufferMap.containsKey(ERROR_CODE)) {
                  String errorCode = bufferMap.get(ERROR_CODE);
                  if (errorCode.equals(ERR_SERVICE_NOT_EXISTS) || errorCode.equals(ERR_SCHEMA_NOT_EXISTS)) {
                    countDownLatch.countDown();
                    return;
                  }
                }
              } catch (IOException e) {
                LOGGER.warn("read value failed from buffer {}", bodyBuffer.toString());
              }
            }

            // no need to support 304 in this place
            if (!HttpStatusClass.SUCCESS.equals(HttpStatusClass.valueOf(response.statusCode()))) {
              LOGGER.warn("get response for {} failed, {}:{}, {}",
                  cls.getName(),
                  response.statusCode(),
                  response.statusMessage(),
                  bodyBuffer.toString());
              countDownLatch.countDown();
              return;
            }

            try {
              holder.value =
                  JsonUtils.readValue(bodyBuffer.getBytes(), cls);
            } catch (Exception e) {
              holder.setStatusCode(0).setThrowable(e);
              LOGGER.warn("read value failed and response message is {}",
                  bodyBuffer.toString());
            }
            countDownLatch.countDown();
          });
    };
  }

  static class ResponseWrapper {
    HttpClientResponse response;

    Buffer bodyBuffer;
  }

  // temporary copy from syncHandler
  // we will use swagger invocation to replace restClientUtil later.
  private Handler<RestResponse> syncHandlerEx(CountDownLatch countDownLatch, Holder<ResponseWrapper> holder) {
    return restResponse -> {
      RequestContext requestContext = restResponse.getRequestContext();
      HttpClientResponse response = restResponse.getResponse();
      if (response == null) {
        // 请求失败，触发请求SC的其他实例
        if (requestContext.getRetryTimes() <= ipPortManager.getMaxRetryTimes()) {
          retry(requestContext, syncHandlerEx(countDownLatch, holder));
        } else {
          countDownLatch.countDown();
        }

        return;
      }
      response.exceptionHandler(e -> {
        LOGGER.error("error in processing response.", e);
        countDownLatch.countDown();
      });
      response.bodyHandler(bodyBuffer -> {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        responseWrapper.response = response;
        responseWrapper.bodyBuffer = bodyBuffer;
        holder.value = responseWrapper;
        countDownLatch.countDown();
      });
    };
  }

  private Handler<RestResponse> syncHandlerForInstances(CountDownLatch countDownLatch,
      MicroserviceInstances mInstances) {
    return restResponse -> {
      RequestContext requestContext = restResponse.getRequestContext();
      HttpClientResponse response = restResponse.getResponse();
      if (response == null) {
        // 请求失败，触发请求SC的其他实例
        if (requestContext.getRetryTimes() <= ipPortManager.getMaxRetryTimes()) {
          retry(requestContext, syncHandlerForInstances(countDownLatch, mInstances));
        } else {
          countDownLatch.countDown();
        }
        return;
      }
      response.exceptionHandler(e -> {
        LOGGER.warn("failed to findInstances.", e);
        countDownLatch.countDown();
      });
      response.bodyHandler(
          bodyBuffer -> {
            try {
              mInstances.setRevision(response.getHeader("X-Resource-Revision"));
              switch (response.statusCode()) {
                case 304:
                  mInstances.setNeedRefresh(false);
                  break;
                case 200:
                  mInstances
                      .setInstancesResponse(JsonUtils.readValue(bodyBuffer.getBytes(), FindInstancesResponse.class));
                  mInstances.setNeedRefresh(true);
                  break;
                case 400: {
                  @SuppressWarnings("unchecked")
                  Map<String, Object> error = JsonUtils.readValue(bodyBuffer.getBytes(), Map.class);
                  if ("400012".equals(error.get("errorCode"))) {
                    mInstances.setMicroserviceNotExist(true);
                    mInstances.setNeedRefresh(false);
                  }
                  LOGGER.warn("failed to findInstances: " + bodyBuffer.toString());
                }
                break;
                default:
                  LOGGER.warn("failed to findInstances: " + bodyBuffer.toString());
                  break;
              }
            } catch (Exception e) {
              LOGGER.warn("read value failed and response message is {}", bodyBuffer.toString());
            }
            countDownLatch.countDown();
          });
    };
  }

  @Override
  public List<Microservice> getAllMicroservices() {
    Holder<GetAllServicesResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    CountDownLatch countDownLatch = new CountDownLatch(1);
    restClientUtil.get(ipPort,
        Const.REGISTRY_API.MICROSERVICE_OPERATION_ALL,
        new RequestParam(),
        syncHandler(countDownLatch, GetAllServicesResponse.class, holder));
    try {
      countDownLatch.await();
      if (holder.value != null) {
        return holder.value.getServices();
      }
    } catch (Exception e) {
      LOGGER.error("query all microservices failed", e);
    }
    return emptyList();
  }

  @Override
  public String getMicroserviceId(String appId, String microserviceName, String versionRule, String environment) {
    Holder<GetExistenceResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    CountDownLatch countDownLatch = new CountDownLatch(1);
    restClientUtil.get(ipPort,
        Const.REGISTRY_API.MICROSERVICE_EXISTENCE,
        new RequestParam().addQueryParam("type", "microservice")
            .addQueryParam("appId", appId)
            .addQueryParam("serviceName", microserviceName)
            .addQueryParam("version", versionRule)
            .addQueryParam("env", environment),
        syncHandler(countDownLatch, GetExistenceResponse.class, holder));
    try {
      countDownLatch.await();
      if (holder.value != null) {
        return holder.value.getServiceId();
      }
    } catch (Exception e) {
      LOGGER.error("query microservice id {}/{}/{} fail",
          appId,
          microserviceName,
          versionRule,
          e);
    }
    return null;
  }

  @Override
  public boolean isSchemaExist(String microserviceId, String schemaId) {
    Holder<GetExistenceResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    CountDownLatch countDownLatch = new CountDownLatch(1);
    restClientUtil.get(ipPort,
        Const.REGISTRY_API.MICROSERVICE_EXISTENCE,
        new RequestParam().addQueryParam("type", "schema")
            .addQueryParam("serviceId", microserviceId)
            .addQueryParam("schemaId", schemaId),
        syncHandler(countDownLatch, GetExistenceResponse.class, holder));
    try {
      countDownLatch.await();
    } catch (Exception e) {
      LOGGER.error("query schema exist {}/{} fail",
          microserviceId,
          schemaId,
          e);
    }
    return holder.value != null && schemaId.equals(holder.value.getSchemaId());
  }

  @Override
  public boolean registerSchema(String microserviceId, String schemaId, String schemaContent) {
    Holder<ResponseWrapper> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    try {
      CreateSchemaRequest request = new CreateSchemaRequest();
      request.setSchema(schemaContent);
      request.setSummary(RegistryUtils.calcSchemaSummary(schemaContent));
      byte[] body = JsonUtils.writeValueAsBytes(request);

      CountDownLatch countDownLatch = new CountDownLatch(1);
      restClientUtil.put(ipPort,
          String.format(Const.REGISTRY_API.MICROSERVICE_SCHEMA, microserviceId, schemaId),
          new RequestParam().setBody(body),
          syncHandlerEx(countDownLatch, holder));
      countDownLatch.await();

      if (holder.value == null) {
        LOGGER.error("Register schema {}/{} failed.", microserviceId, schemaId);
        return false;
      }

      if (!Status.Family.SUCCESSFUL.equals(Status.Family.familyOf(holder.value.response.statusCode()))) {
        LOGGER.error("Register schema {}/{} failed, statusCode: {}, statusMessage: {}, description: {}.",
            microserviceId,
            schemaId,
            holder.value.response.statusCode(),
            holder.value.response.statusMessage(),
            holder.value.bodyBuffer.toString());
        return false;
      }

      LOGGER.info("register schema {}/{} success.",
          microserviceId,
          schemaId);
      return true;
    } catch (Exception e) {
      LOGGER.error("register schema {}/{} fail.",
          microserviceId,
          schemaId,
          e);
    }
    return false;
  }

  @Override
  public String getSchema(String microserviceId, String schemaId) {
    return doGetSchema(microserviceId, schemaId, false);
  }

  private String doGetSchema(String microserviceId, String schemaId, boolean global) {
    try {
      // avoid query too many times of schema when first time loading
      String cachedSchema = schemaCache.get(microserviceId).get(schemaId);
      if (cachedSchema != null) {
        return cachedSchema;
      }
    } catch (ExecutionException e) {
      // ignore this error.
    }

    Holder<GetSchemaResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    CountDownLatch countDownLatch = new CountDownLatch(1);
    RequestParam param = new RequestParam();
    if (global) {
      param.addQueryParam("global", "true");
    }
    restClientUtil.get(ipPort,
        String.format(Const.REGISTRY_API.MICROSERVICE_SCHEMA, microserviceId, schemaId),
        param,
        syncHandler(countDownLatch, GetSchemaResponse.class, holder));
    try {
      countDownLatch.await();
    } catch (Exception e) {
      LOGGER.error("query schema exist {}/{} failed",
          schemaId,
          e);
    }
    if (holder.value != null) {
      return holder.value.getSchema();
    }

    return null;
  }

  @Override
  public String getAggregatedSchema(String microserviceId, String schemaId) {
    return doGetSchema(microserviceId, schemaId, true);
  }

  @Override
  public Holder<List<GetSchemaResponse>> getSchemas(String microserviceId) {
    return getSchemas(microserviceId, false, false);
  }

  private Holder<List<GetSchemaResponse>> getSchemas(String microserviceId, boolean withSchema, boolean global) {
    Holder<GetSchemasResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();
    Holder<List<GetSchemaResponse>> resultHolder = new Holder<>();

    CountDownLatch countDownLatch = new CountDownLatch(1);
    String url = Const.REGISTRY_API.MICROSERVICE_ALL_SCHEMAs;
    RequestParam requestParam = new RequestParam();
    if (withSchema) {
      url = Const.REGISTRY_API.MICROSERVICE_ALL_SCHEMAs + "?withSchema=1";
    }
    if (global) {
      requestParam.addQueryParam("global", "true");
    }

    restClientUtil.get(ipPort,
        String.format(url, microserviceId),
        requestParam,
        syncHandler(countDownLatch, GetSchemasResponse.class, holder));
    try {
      countDownLatch.await();
    } catch (Exception e) {
      LOGGER.error("query all schemas {} failed",
          microserviceId,
          e);
    }
    resultHolder.setStatusCode(holder.getStatusCode()).setThrowable(holder.getThrowable());
    if (holder.value != null) {
      return resultHolder.setValue(
          holder.value.getSchema() != null ?
              holder.value.getSchema() :
              holder.value.getSchemas());
    }

    return resultHolder;
  }

  @Override
  public String registerMicroservice(Microservice microservice) {
    Holder<CreateServiceResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();
    try {
      CreateServiceRequest request = new CreateServiceRequest();
      request.setService(microservice);
      byte[] body = JsonUtils.writeValueAsBytes(request);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("register microservice: {}", new String(body, Charset.defaultCharset()));
      }

      CountDownLatch countDownLatch = new CountDownLatch(1);
      restClientUtil.post(ipPort,
          Const.REGISTRY_API.MICROSERVICE_OPERATION_ALL,
          new RequestParam().setBody(body),
          syncHandler(countDownLatch, CreateServiceResponse.class, holder));
      countDownLatch.await();
      if (holder.value != null) {
        return holder.value.getServiceId();
      }
    } catch (Exception e) {
      LOGGER.error("register microservice {}/{}/{} failed",
          microservice.getAppId(),
          microservice.getServiceName(),
          microservice.getVersion(),
          e);
    }
    return null;
  }

  @Override
  public Microservice getMicroservice(String microserviceId) {
    return doGetMicroservice(microserviceId, false);
  }

  private Microservice doGetMicroservice(String microserviceId, boolean global) {
    Holder<GetServiceResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    CountDownLatch countDownLatch = new CountDownLatch(1);
    RequestParam param = new RequestParam();
    if (global) {
      param.addQueryParam("global", "true");
    }
    restClientUtil.get(ipPort,
        String.format(Const.REGISTRY_API.MICROSERVICE_OPERATION_ONE, microserviceId),
        param,
        syncHandler(countDownLatch, GetServiceResponse.class, holder));
    try {
      countDownLatch.await();
      if (holder.value != null) {
        return holder.value.getService();
      }
    } catch (Exception e) {
      LOGGER.error("query microservice {} failed", microserviceId, e);
    }
    return null;
  }

  @Override
  public Microservice getAggregatedMicroservice(String microserviceId) {
    return doGetMicroservice(microserviceId, true);
  }

  @Override
  public String registerMicroserviceInstance(MicroserviceInstance instance) {
    Holder<RegisterInstanceResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    try {
      RegisterInstanceRequest request = new RegisterInstanceRequest();
      request.setInstance(instance);
      byte[] body = JsonUtils.writeValueAsBytes(request);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("register microservice: {}", new String(body, Charset.defaultCharset()));
      }

      CountDownLatch countDownLatch = new CountDownLatch(1);
      restClientUtil.post(ipPort,
          String.format(Const.REGISTRY_API.MICROSERVICE_INSTANCE_OPERATION_ALL, instance.getServiceId()),
          new RequestParam().setBody(body),
          syncHandler(countDownLatch, RegisterInstanceResponse.class, holder));
      countDownLatch.await();
      if (holder.value != null) {
        return holder.value.getInstanceId();
      }
    } catch (Exception e) {
      LOGGER.error("register microservice instance {} failed", instance.getServiceId(), e);
    }
    return null;
  }

  @Override
  public List<MicroserviceInstance> getMicroserviceInstance(String consumerId, String providerId) {
    Holder<GetInstancesResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    CountDownLatch countDownLatch = new CountDownLatch(1);
    restClientUtil.get(ipPort,
        String.format(Const.REGISTRY_API.MICROSERVICE_INSTANCE_OPERATION_ALL, providerId),
        new RequestParam().addHeader("X-ConsumerId", consumerId),
        syncHandler(countDownLatch, GetInstancesResponse.class, holder));
    try {
      countDownLatch.await();
      if (holder.value != null) {
        return holder.value.getInstances();
      }
    } catch (Exception e) {
      LOGGER.error("query microservice instances {} failed", providerId, e);
    }
    return null;
  }

  @Override
  public boolean unregisterMicroserviceInstance(String microserviceId, String microserviceInstanceId) {
    Holder<HttpClientResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    CountDownLatch countDownLatch = new CountDownLatch(1);
    restClientUtil.delete(ipPort,
        String.format(Const.REGISTRY_API.MICROSERVICE_INSTANCE_OPERATION_ONE, microserviceId, microserviceInstanceId),
        new RequestParam(),
        syncHandler(countDownLatch, HttpClientResponse.class, holder));
    try {
      countDownLatch.await();
      if (holder.value != null) {
        if (holder.value.statusCode() == Status.OK.getStatusCode()) {
          return true;
        }
        LOGGER.warn(holder.value.statusMessage());
      }
    } catch (Exception e) {
      LOGGER.error("unregister microservice instance {}/{} failed",
          microserviceId,
          microserviceInstanceId,
          e);
    }
    return false;
  }

  @Override
  public HeartbeatResponse heartbeat(String microserviceId, String microserviceInstanceId) {
    Holder<HttpClientResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    CountDownLatch countDownLatch = new CountDownLatch(1);
    restClientUtil.put(ipPort,
        String.format(Const.REGISTRY_API.MICROSERVICE_HEARTBEAT, microserviceId, microserviceInstanceId),
        new RequestParam().setTimeout(ServiceRegistryConfig.INSTANCE.getHeartBeatRequestTimeout()),
        syncHandler(countDownLatch, HttpClientResponse.class, holder));

    try {
      countDownLatch.await();
      if (holder.value != null) {
        HeartbeatResponse response = new HeartbeatResponse();
        response.setMessage(holder.value.statusMessage());
        if (holder.value.statusCode() == Status.OK.getStatusCode()) {
          response.setOk(true);
          return response;
        }
        LOGGER.warn(holder.value.statusMessage());
        return response;
      }
    } catch (Exception e) {
      LOGGER.error("update microservice instance {}/{} heartbeat failed",
          microserviceId,
          microserviceInstanceId,
          e);
    }
    return null;
  }

  public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback) {
    watch(selfMicroserviceId, callback, v -> {
    }, v -> {
    });
  }

  public void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback,
      AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose) {
    Boolean alreadyWatch = watchServices.get(selfMicroserviceId);
    if (alreadyWatch == null) {
      synchronized (ServiceRegistryClientImpl.class) {
        alreadyWatch = watchServices.get(selfMicroserviceId);
        if (alreadyWatch == null) {
          watchServices.put(selfMicroserviceId, true);

          String url = String.format(Const.REGISTRY_API.MICROSERVICE_WATCH, selfMicroserviceId);

          IpPort ipPort = ipPortManager.getAvailableAddress();
          websocketClientUtil.open(ipPort, url, o -> {
            onOpen.success(o);
            LOGGER.info(
                "watching microservice {} successfully, "
                    + "the chosen service center address is {}:{}",
                selfMicroserviceId,
                ipPort.getHostOrIp(),
                ipPort.getPort());
          }, c -> {
            watchErrorHandler(new ClientException("connection is closed accidentally"),
                selfMicroserviceId,
                callback);
            onClose.success(null);
          }, bodyBuffer -> {
            MicroserviceInstanceChangedEvent response;
            try {
              response = JsonUtils.readValue(bodyBuffer.getBytes(),
                  MicroserviceInstanceChangedEvent.class);
            } catch (Exception e) {
              LOGGER.error("watcher handle microservice {} response failed, {}",
                  selfMicroserviceId,
                  bodyBuffer.toString());
              return;
            }
            try {
              callback.success(response);
            } catch (Exception e) {
              LOGGER.error("notify watcher failed, microservice {}",
                  selfMicroserviceId,
                  e);
            }
          }, e -> {
            watchErrorHandler(e, selfMicroserviceId, callback);
            onClose.success(null);
          }, f -> {
            watchErrorHandler(f, selfMicroserviceId, callback);
          });
        }
      }
    }
  }

  @Override
  public List<MicroserviceInstance> findServiceInstance(String consumerId, String appId, String serviceName,
      String versionRule) {
    MicroserviceInstances instances = findServiceInstances(consumerId, appId, serviceName, versionRule, null);
    if (instances == null) {
      return null;
    }
    return instances.getInstancesResponse().getInstances();
  }

  @Override
  public MicroserviceInstances findServiceInstances(String consumerId, String appId, String serviceName,
      String versionRule, String revision) {
    MicroserviceInstances microserviceInstances = new MicroserviceInstances();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    CountDownLatch countDownLatch = new CountDownLatch(1);

    RequestParam requestParam = new RequestParam().addQueryParam("appId", appId)
        .addQueryParam("serviceName", serviceName)
        .addQueryParam("global", "true")
        .addQueryParam("version", versionRule);
    if (RegistryUtils.getMicroservice().getEnvironment() != null) {
      requestParam.addQueryParam("env", RegistryUtils.getMicroservice().getEnvironment());
    }
    if (consumerId != null) {
      requestParam.addHeader("X-ConsumerId", consumerId);
    }
    if (revision != null) {
      requestParam.addQueryParam("rev", revision);
    }

    restClientUtil.get(ipPort,
        Const.REGISTRY_API.MICROSERVICE_INSTANCES,
        requestParam,
        syncHandlerForInstances(countDownLatch, microserviceInstances));
    try {
      countDownLatch.await();
      if (!microserviceInstances.isNeedRefresh()) {
        return microserviceInstances;
      }
      if (microserviceInstances.getInstancesResponse() == null) {
        return null; // error
      }
      List<MicroserviceInstance> list = microserviceInstances.getInstancesResponse().getInstances();
      if (list == null) {
        microserviceInstances.getInstancesResponse().setInstances(new ArrayList<>());
      }
      return microserviceInstances;
    } catch (Exception e) {
      LOGGER.error("find microservice instance {}/{}/{} failed",
          appId,
          serviceName,
          versionRule,
          e);
    }
    return null;
  }

  private void watchErrorHandler(Throwable e, String selfMicroserviceId,
      AsyncResultCallback<MicroserviceInstanceChangedEvent> callback) {
    LOGGER.error(
        "watcher connect to service center server failed, microservice {}, {}",
        selfMicroserviceId,
        e.getMessage());
    callback.fail(e);
    watchServices.remove(selfMicroserviceId);
  }

  @Override
  public boolean updateMicroserviceProperties(String microserviceId, Map<String, String> serviceProperties) {
    Holder<HttpClientResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    try {
      UpdatePropertiesRequest request = new UpdatePropertiesRequest();
      request.setProperties(serviceProperties);
      byte[] body = JsonUtils.writeValueAsBytes(request);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("update properties of microservice: {}", new String(body, Charset.defaultCharset()));
      }

      CountDownLatch countDownLatch = new CountDownLatch(1);
      restClientUtil.put(ipPort,
          String.format(Const.REGISTRY_API.MICROSERVICE_PROPERTIES, microserviceId),
          new RequestParam().setBody(body),
          syncHandler(countDownLatch, HttpClientResponse.class, holder));

      countDownLatch.await();
      if (holder.value != null) {
        if (holder.value.statusCode() == Status.OK.getStatusCode()) {
          return true;
        }
        LOGGER.warn(holder.value.statusMessage());
      }
    } catch (Exception e) {
      LOGGER.error("update properties of microservice {} failed",
          microserviceId,
          e);
    }
    return false;
  }

  @Override
  public boolean updateInstanceProperties(String microserviceId, String microserviceInstanceId,
      Map<String, String> instanceProperties) {
    Holder<HttpClientResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    try {
      UpdatePropertiesRequest request = new UpdatePropertiesRequest();
      request.setProperties(instanceProperties);
      byte[] body = JsonUtils.writeValueAsBytes(request);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("update properties of microservice instance: {}",
            new String(body, Charset.defaultCharset()));
      }

      CountDownLatch countDownLatch = new CountDownLatch(1);
      restClientUtil.put(ipPort,
          String.format(Const.REGISTRY_API.MICROSERVICE_INSTANCE_PROPERTIES, microserviceId, microserviceInstanceId),
          new RequestParam().setBody(body),
          syncHandler(countDownLatch, HttpClientResponse.class, holder));

      countDownLatch.await();
      if (holder.value != null) {
        if (holder.value.statusCode() == Status.OK.getStatusCode()) {
          return true;
        }
        LOGGER.warn(holder.value.statusMessage());
      }
    } catch (Exception e) {
      LOGGER.error("update properties of microservice instance {}/{} failed",
          microserviceId,
          microserviceInstanceId,
          e);
    }
    return false;
  }

  @Override
  public MicroserviceInstance findServiceInstance(String serviceId, String instanceId) {
    try {
      Holder<MicroserviceInstanceResponse> holder = new Holder<>();
      IpPort ipPort = ipPortManager.getAvailableAddress();
      CountDownLatch countDownLatch = new CountDownLatch(1);
      restClientUtil.get(ipPort,
          String.format(Const.REGISTRY_API.MICROSERVICE_INSTANCE_OPERATION_ONE, serviceId, instanceId),
          new RequestParam().addHeader("X-ConsumerId", serviceId).addQueryParam("global", "true"),
          syncHandler(countDownLatch, MicroserviceInstanceResponse.class, holder));
      countDownLatch.await();
      if (null != holder.value) {
        return holder.value.getInstance();
      }
      return null;
    } catch (Exception e) {
      LOGGER.error("get instance from sc failed");
      return null;
    }
  }

  @Override
  public ServiceCenterInfo getServiceCenterInfo() {
    Holder<ServiceCenterInfo> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();

    CountDownLatch countDownLatch = new CountDownLatch(1);
    restClientUtil.get(ipPort,
        Const.REGISTRY_API.SERVICECENTER_VERSION,
        new RequestParam(),
        syncHandler(countDownLatch, ServiceCenterInfo.class, holder));
    try {
      countDownLatch.await();
      if (holder.value != null) {
        return holder.value;
      }
    } catch (Exception e) {
      LOGGER.error("query servicecenter version info failed.", e);
    }
    return null;
  }

  @Override
  public boolean updateMicroserviceInstanceStatus(String microserviceId, String instanceId,
      MicroserviceInstanceStatus status) {
    if (null == status) {
      throw new IllegalArgumentException("null status is now allowed");
    }

    Holder<HttpClientResponse> holder = new Holder<>();
    IpPort ipPort = ipPortManager.getAvailableAddress();
    try {
      LOGGER.debug("update status of microservice instance: {}", status);
      String url = String.format(Const.REGISTRY_API.MICROSERVICE_INSTANCE_STATUS, microserviceId, instanceId);
      Map<String, String[]> queryParams = new HashMap<>();
      queryParams.put("value", new String[] {status.toString()});
      CountDownLatch countDownLatch = new CountDownLatch(1);
      restClientUtil.put(ipPort, url, new RequestParam().setQueryParams(queryParams),
          syncHandler(countDownLatch, HttpClientResponse.class, holder));
      countDownLatch.await();
      if (holder.value != null) {
        if (holder.value.statusCode() == Status.OK.getStatusCode()) {
          return true;
        }
        LOGGER.warn(holder.value.statusMessage());
      }
    } catch (Exception e) {
      LOGGER.error("update status of microservice instance {}/{} failed",
          microserviceId,
          instanceId,
          e);
    }
    return false;
  }

  @Subscribe
  public void onMicroserviceHeartbeatTask(MicroserviceInstanceHeartbeatTask event) {
    if (HeartbeatResult.SUCCESS.equals(event.getHeartbeatResult())) {
      ipPortManager.initAutoDiscovery();
    }
  }
}
