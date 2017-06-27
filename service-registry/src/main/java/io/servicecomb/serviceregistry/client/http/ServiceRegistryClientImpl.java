/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.client.http;

import static io.servicecomb.serviceregistry.api.Const.EXISTENCE_PATH;
import static io.servicecomb.serviceregistry.api.Const.HEARTBEAT_PATH;
import static io.servicecomb.serviceregistry.api.Const.INSTANCES_PATH;
import static io.servicecomb.serviceregistry.api.Const.MICROSERVICE_PATH;
import static io.servicecomb.serviceregistry.api.Const.MS_API_PATH;
import static io.servicecomb.serviceregistry.api.Const.PROPERTIES_PATH;
import static io.servicecomb.serviceregistry.api.Const.SCHEMA_PATH;
import static io.servicecomb.serviceregistry.api.Const.WATCHER_PATH;
import static java.util.Collections.emptyList;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import javax.ws.rs.core.Response.Status;
import javax.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.foundation.common.net.IpPort;
import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.foundation.vertx.AsyncResultCallback;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.request.CreateSchemaRequest;
import io.servicecomb.serviceregistry.api.request.CreateServiceRequest;
import io.servicecomb.serviceregistry.api.request.RegisterInstanceRequest;
import io.servicecomb.serviceregistry.api.request.UpdatePropertiesRequest;
import io.servicecomb.serviceregistry.api.response.CreateServiceResponse;
import io.servicecomb.serviceregistry.api.response.FindInstancesResponse;
import io.servicecomb.serviceregistry.api.response.GetAllServicesResponse;
import io.servicecomb.serviceregistry.api.response.GetExistenceResponse;
import io.servicecomb.serviceregistry.api.response.GetInstancesResponse;
import io.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import io.servicecomb.serviceregistry.api.response.GetServiceResponse;
import io.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import io.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import io.servicecomb.serviceregistry.api.response.RegisterInstanceResponse;
import io.servicecomb.serviceregistry.client.ClientException;
import io.servicecomb.serviceregistry.client.IpPortManager;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientResponse;

public final class ServiceRegistryClientImpl implements ServiceRegistryClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryClientImpl.class);

    private IpPortManager ipPortManager;

    // key是本进程的微服务id和服务管理中心的id
    // extract this, ServiceRegistryClient is better to be no status.
    private Map<String, Boolean> watchServices = new ConcurrentHashMap<>();

    public ServiceRegistryClientImpl(IpPortManager ipPortManager) {
        this.ipPortManager = ipPortManager;
    }

    @Override
    public void init() {
    }

    private boolean retry(RequestContext requestContext, Handler<RestResponse> responseHandler) {
        IpPort ipPort = ipPortManager.next();
        if (ipPort == null) {
            return false;
        }
        requestContext.setIpPort(ipPortManager.get());
        RestUtils.httpDo(requestContext, responseHandler);
        return true;
    }

    @SuppressWarnings("unchecked")
    private <T> Handler<RestResponse> syncHandler(CountDownLatch countDownLatch, Class<T> cls,
            Holder<T> holder) {
        return restResponse -> {
            RequestContext requestContext = restResponse.getRequestContext();
            HttpClientResponse response = restResponse.getResponse();
            if (response == null) {
                // 请求失败，触发请求SC的其他实例
                if (!retry(requestContext, syncHandler(countDownLatch, cls, holder))) {
                    countDownLatch.countDown();
                }
                return;
            }
            response.bodyHandler(
                    bodyBuffer -> {
                        if (cls.getName().equals(HttpClientResponse.class.getName())) {
                            holder.value = (T) response;
                            countDownLatch.countDown();
                            return;
                        }
                        try {
                            holder.value =
                                JsonUtils.readValue(bodyBuffer.getBytes(), cls);
                        } catch (Exception e) {
                            LOGGER.warn(bodyBuffer.toString());
                        }
                        countDownLatch.countDown();
                    });
        };
    }

    @Override
    public List<Microservice> getAllMicroservices() {
        Holder<GetAllServicesResponse> holder = new Holder<>();
        IpPort ipPort = ipPortManager.get();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        RestUtils.get(ipPort,
                MS_API_PATH + MICROSERVICE_PATH,
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
    public String getMicroserviceId(String appId, String microserviceName, String versionRule) {
        Holder<GetExistenceResponse> holder = new Holder<>();
        IpPort ipPort = ipPortManager.get();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        RestUtils.get(ipPort,
                MS_API_PATH + EXISTENCE_PATH,
                new RequestParam().addQueryParam("type", "microservice")
                        .addQueryParam("appId", appId)
                        .addQueryParam("serviceName", microserviceName)
                        .addQueryParam("version", versionRule),
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
        IpPort ipPort = ipPortManager.get();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        RestUtils.get(ipPort,
                MS_API_PATH + EXISTENCE_PATH,
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
        return holder.value != null;
    }

    @Override
    public boolean registerSchema(String microserviceId, String schemaId, String schemaContent) {
        Holder<HttpClientResponse> holder = new Holder<>();
        IpPort ipPort = ipPortManager.get();

        try {
            CreateSchemaRequest request = new CreateSchemaRequest();
            request.setSchema(schemaContent);
            byte[] body = JsonUtils.writeValueAsBytes(request);

            CountDownLatch countDownLatch = new CountDownLatch(1);
            RestUtils.put(ipPort,
                    MS_API_PATH + MICROSERVICE_PATH + "/" + microserviceId + SCHEMA_PATH + "/" + schemaId,
                    new RequestParam().setBody(body),
                    syncHandler(countDownLatch, HttpClientResponse.class, holder));
            countDownLatch.await();

            boolean result = false;
            if (holder.value != null) {
                result = holder.value.statusCode() == Status.OK.getStatusCode();
            }

            LOGGER.info("register schema {}/{}, result {}",
                    microserviceId,
                    schemaId,
                    result);

            return result;
        } catch (Exception e) {
            LOGGER.error("register schema {}/{} fail",
                    microserviceId,
                    schemaId,
                    e);
        }
        return false;
    }

    @Override
    public String getSchema(String microserviceId, String schemaId) {
        Holder<GetSchemaResponse> holder = new Holder<>();
        IpPort ipPort = ipPortManager.get();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        RestUtils.get(ipPort,
                MS_API_PATH + MICROSERVICE_PATH + "/" + microserviceId + SCHEMA_PATH + "/" + schemaId,
                new RequestParam(),
                syncHandler(countDownLatch, GetSchemaResponse.class, holder));
        try {
            countDownLatch.await();
        } catch (Exception e) {
            LOGGER.error("query schema exist {}/{} failed",
                    microserviceId,
                    schemaId,
                    e);
        }
        if (holder.value != null) {
            return holder.value.getSchema();
        }

        return null;
    }

    @Override
    public String registerMicroservice(Microservice microservice) {
        Holder<CreateServiceResponse> holder = new Holder<>();
        IpPort ipPort = ipPortManager.get();
        try {
            CreateServiceRequest request = new CreateServiceRequest();
            request.setService(microservice);
            byte[] body = JsonUtils.writeValueAsBytes(request);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("register microservice: {}", new String(body, Charset.defaultCharset()));
            }

            CountDownLatch countDownLatch = new CountDownLatch(1);
            RestUtils.post(ipPort,
                    MS_API_PATH + MICROSERVICE_PATH,
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
        Holder<GetServiceResponse> holder = new Holder<>();
        IpPort ipPort = ipPortManager.get();

        StringBuilder url = new StringBuilder(MS_API_PATH);
        url.append(MICROSERVICE_PATH).append("/").append(microserviceId);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        RestUtils.get(ipPort,
                url.toString(),
                new RequestParam(),
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
    public String registerMicroserviceInstance(MicroserviceInstance instance) {
        Holder<RegisterInstanceResponse> holder = new Holder<>();
        IpPort ipPort = ipPortManager.get();

        StringBuilder url = new StringBuilder(MS_API_PATH);
        url.append(MICROSERVICE_PATH)
                .append("/")
                .append(instance.getServiceId())
                .append(INSTANCES_PATH);

        try {
            RegisterInstanceRequest request = new RegisterInstanceRequest();
            request.setInstance(instance);
            byte[] body = JsonUtils.writeValueAsBytes(request);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("register microservice: {}", new String(body, Charset.defaultCharset()));
            }

            CountDownLatch countDownLatch = new CountDownLatch(1);
            RestUtils.post(ipPort,
                    url.toString(),
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
        IpPort ipPort = ipPortManager.get();

        StringBuilder url = new StringBuilder(MS_API_PATH);
        url.append(MICROSERVICE_PATH)
                .append("/")
                .append(providerId)
                .append(INSTANCES_PATH);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        RestUtils.get(ipPort,
                url.toString(),
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
        IpPort ipPort = ipPortManager.get();

        StringBuilder url = new StringBuilder(MS_API_PATH);
        url.append(MICROSERVICE_PATH)
                .append("/")
                .append(microserviceId)
                .append(INSTANCES_PATH)
                .append("/")
                .append(microserviceInstanceId);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        RestUtils.delete(ipPort,
                url.toString(),
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
        IpPort ipPort = ipPortManager.get();

        StringBuilder url = new StringBuilder(MS_API_PATH);
        url.append(MICROSERVICE_PATH)
                .append("/")
                .append(microserviceId)
                .append(INSTANCES_PATH)
                .append("/")
                .append(microserviceInstanceId)
                .append(HEARTBEAT_PATH);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        RestUtils.put(ipPort,
                url.toString(),
                new RequestParam(),
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

                    String url = MS_API_PATH + MICROSERVICE_PATH + "/" + selfMicroserviceId + WATCHER_PATH;

                    IpPort ipPort = ipPortManager.get();
                    if (ipPort == null) {
                        LOGGER.error("request address is null, watch microservice {}",
                                selfMicroserviceId);
                        watchErrorHandler(new Exception("request address is null"),
                                selfMicroserviceId,
                                callback);
                        return;
                    }
                    WebsocketUtils.open(ipPort, url, o -> {
                        onOpen.success(o);
                        LOGGER.info(
                                "watching microservice {} successfully, "
                                        + "the chosen service center address is {}:{}",
                                selfMicroserviceId,
                                ipPort.getHostOrIp(),
                                ipPort.getPort());
                    }, c -> {
                        LOGGER.warn(
                                "watching microservice {} connection is closed accidentally",
                                selfMicroserviceId);
                        watchErrorHandler(new ClientException("connection is closed accidentally"),
                                selfMicroserviceId,
                                callback);

                        onClose.success(null);
                    }, bodyBuffer -> {

                        MicroserviceInstanceChangedEvent response = null;
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
                        LOGGER.error(
                                "watcher read microservice {} message from service center failed,"
                                        + " {}",
                                selfMicroserviceId,
                                e.getMessage());
                    }, f -> {

                        if (!watchServices.containsKey(selfMicroserviceId)) {
                            return;
                        }
                        LOGGER.error(
                                "watcher connect to service center server failed, microservice {}, {}",
                                selfMicroserviceId,
                                f.getMessage());
                        watchErrorHandler(f, selfMicroserviceId, callback);
                    });
                }
            }
        }
    }

    @Override
    public List<MicroserviceInstance> findServiceInstance(String consumerId, String appId, String serviceName,
            String versionRule) {
        Holder<FindInstancesResponse> holder = new Holder<>();
        IpPort ipPort = ipPortManager.get();

        StringBuilder url = new StringBuilder(MS_API_PATH);
        url.append(INSTANCES_PATH);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        RestUtils.get(ipPort,
                url.toString(),
                new RequestParam().addQueryParam("appId", appId)
                        .addQueryParam("serviceName", serviceName)
                        .addQueryParam("version", versionRule)
                        .addHeader("X-ConsumerId", consumerId),
                syncHandler(countDownLatch, FindInstancesResponse.class, holder));
        try {
            countDownLatch.await();
            if (holder.value == null) {
                return null; // error

            }
            List<MicroserviceInstance> list = holder.value.getInstances();
            if (list == null) {
                return new ArrayList<>();
            }
            return list;
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
        callback.fail(e);
        watchServices.remove(selfMicroserviceId);
    }

    @Override
    public boolean updateMicroserviceProperties(String microserviceId, Map<String, String> serviceProperties) {
        Holder<HttpClientResponse> holder = new Holder<>();
        IpPort ipPort = ipPortManager.get();

        StringBuilder url = new StringBuilder(MS_API_PATH);
        url.append(MICROSERVICE_PATH)
                .append("/")
                .append(microserviceId)
                .append(PROPERTIES_PATH);

        try {
            UpdatePropertiesRequest request = new UpdatePropertiesRequest();
            request.setProperties(serviceProperties);
            byte[] body = JsonUtils.writeValueAsBytes(request);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("update properties of microservice: {}", new String(body, Charset.defaultCharset()));
            }

            CountDownLatch countDownLatch = new CountDownLatch(1);
            RestUtils.put(ipPort,
                    url.toString(),
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
        IpPort ipPort = ipPortManager.get();

        StringBuilder url = new StringBuilder(MS_API_PATH);
        url.append(MICROSERVICE_PATH)
                .append("/")
                .append(microserviceId)
                .append(INSTANCES_PATH)
                .append("/")
                .append(microserviceInstanceId)
                .append(PROPERTIES_PATH);

        try {
            UpdatePropertiesRequest request = new UpdatePropertiesRequest();
            request.setProperties(instanceProperties);
            byte[] body = JsonUtils.writeValueAsBytes(request);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("update properties of microservice instance: {}",
                        new String(body, Charset.defaultCharset()));
            }

            CountDownLatch countDownLatch = new CountDownLatch(1);
            RestUtils.put(ipPort,
                    url.toString(),
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
}
