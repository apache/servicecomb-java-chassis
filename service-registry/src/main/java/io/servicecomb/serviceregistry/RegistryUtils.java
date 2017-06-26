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

package io.servicecomb.serviceregistry;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.foundation.common.CommonThread;
import io.servicecomb.foundation.common.net.IpPort;
import io.servicecomb.foundation.common.net.NetUtils;
import io.servicecomb.serviceregistry.api.Const;
import io.servicecomb.serviceregistry.api.registry.BasePath;
import io.servicecomb.serviceregistry.api.registry.HealthCheck;
import io.servicecomb.serviceregistry.api.registry.HealthCheckMode;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import io.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import io.servicecomb.serviceregistry.cache.CacheRegistryListener;
import io.servicecomb.serviceregistry.cache.InstanceCacheManager;
import io.servicecomb.serviceregistry.cache.InstanceVersionCacheManager;
import io.servicecomb.serviceregistry.client.ClientException;
import io.servicecomb.serviceregistry.client.RegistryClientFactory;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;
import io.servicecomb.serviceregistry.config.InstancePropertiesLoader;
import io.servicecomb.serviceregistry.config.MicroservicePropertiesLoader;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.servicecomb.serviceregistry.notify.NotifyManager;
import io.servicecomb.serviceregistry.notify.NotifyThread;
import io.servicecomb.serviceregistry.notify.RegistryEvent;
import io.servicecomb.serviceregistry.notify.RegistryListener;
import io.servicecomb.serviceregistry.utils.Timer;
import io.servicecomb.serviceregistry.utils.TimerException;

public final class RegistryUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryUtils.class);

    // 本进程的描述
    private static Microservice microservice;

    private static MicroserviceInstance microserviceInstance;

    private static ServiceRegistryClient srClient;

    private static ServiceRegistryConfig serviceRegistryConfig;

    private static final String DEFAULT_STAGE = "prod";

    private static final String ALLOW_CROSS_APP_KEY = "allowCrossApp";

    private static final String DEFAULT_PATH_CHECKSESSION = "false";

    // value is ip or {interface name}
    public static final String PUBLISH_ADDRESS = "cse.service.publishAddress";

    private static final String PUBLISH_PORT = "cse.{transport_name}.publishPort";

    private static CommonThread registryThread = null;

    private static boolean isUnavailabe = false;

    private RegistryUtils() {
    }

    public static Microservice getMicroservice() {
        if (microservice == null) {
            microservice = createMicroserviceFromDefinition();
        }

        return microservice;
    }

    public static MicroserviceInstance getMicroserviceInstance() {
        if (microserviceInstance == null) {
            microserviceInstance = createMicroserviceInstance();
        }

        return microserviceInstance;
    }

    private static Microservice createMicroserviceFromDefinition() {
        Microservice microservice = new Microservice();
        String name = DynamicPropertyFactory.getInstance().getStringProperty("service_description.name", null).get();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("You must configure service_description.name");
        }
        microservice.setServiceName(name);
        microservice.setAppId(
                DynamicPropertyFactory.getInstance().getStringProperty("APPLICATION_ID", "default").get());
        microservice.setVersion(
                DynamicPropertyFactory.getInstance().getStringProperty("service_description.version", "1.0.0").get());
        microservice.setDescription(
                DynamicPropertyFactory.getInstance().getStringProperty("service_description.description", "").get());
        microservice.setLevel(
                DynamicPropertyFactory.getInstance().getStringProperty("service_description.role", "FRONT").get());

        Map<String, String> propertiesMap = MicroservicePropertiesLoader.INSTANCE.loadProperties();
        if (!isEmpty(propertiesMap)) {
            microservice.setProperties(propertiesMap);

            // 当允许跨app调用时为服务设置一个别名
            if (allowCrossApp(propertiesMap)) {
                microservice.setAlias(Microservice.generateAbsoluteMicroserviceName(microservice.getAppId(),
                        microservice.getServiceName()));
            }
        }

        return microservice;
    }

    private static boolean isEmpty(Map<String, String> map) {
        return map == null || map.isEmpty();
    }

    protected static boolean allowCrossApp(Map<String, String> propertiesMap) {
        return Boolean.valueOf(propertiesMap.get(ALLOW_CROSS_APP_KEY));
    }

    private static MicroserviceInstance createMicroserviceInstance() {
        MicroserviceInstance microserviceInstance = new MicroserviceInstance();
        microserviceInstance.setStage(DEFAULT_STAGE);
        Map<String, String> propertiesMap = InstancePropertiesLoader.INSTANCE.loadProperties();
        if (!isEmpty(propertiesMap)) {
            microserviceInstance.setProperties(propertiesMap);
        }

        //This is not the local mode for the development so we need to set the healthcheck
        if (RegistryClientFactory.getLocalModeFile().isEmpty()) {
            HealthCheck healthCheck = new HealthCheck();
            healthCheck.setMode(HealthCheckMode.HEARTBEAT);
            microserviceInstance.setHealthCheck(healthCheck);
        }

        return microserviceInstance;
    }

    /**
     * This method is written so that the UT of this class can be done appropriately.
     */
    public static void setSrClient(ServiceRegistryClient oServiceRegistryClient) {
        srClient = oServiceRegistryClient;
    }

    public static void init() {
        serviceRegistryConfig = ServiceRegistryConfig.INSTANCE;
        if (null == srClient) {
            srClient = RegistryClientFactory.getRegistryClient(); //Changes has been done to test the code properly
        }

        // 初始化vertx & sr client
        srClient.init();

        loadStaticConfiguration();

        new CacheRegistryListener();

        new NotifyThread().start();

        // 初始化先尝试注册一次，轮询一次服务中心所有静态配置地址
        // 如果失败，由RegistryThread保证注册成功
        if (regsiterMicroservice()) {
            regsiterInstance();
        }
        // 本地开发模式下不启动RegistryThread
        if (RegistryClientFactory.getLocalModeFile().isEmpty()) {
            registryThread = new RegistryThread();
            registryThread.start();
        }
    }

    private static void loadStaticConfiguration() {
        // TODO 如果yaml定义了paths规则属性，替换默认值，现需要DynamicPropertyFactory支持数组获取
        List<BasePath> paths = getMicroservice().getPaths();
        for (BasePath path : paths) {
            if (path.getProperty() == null) {
                path.setProperty(new HashMap<>());
            }
            path.getProperty().put(Const.PATH_CHECKSESSION, DEFAULT_PATH_CHECKSESSION);
        }
    }

    public static void ensureRegisterInstance() throws TimerException {
        // 确保注册成功
        Timer timer = Timer.newForeverTimer();
        while (true) {
            if (regsiterInstance()) {
                break;
            }

            timer.sleep();
        }
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean regsiterInstance() {
        String hostName = "";
        if (serviceRegistryConfig.isPreferIpAddress()) {
            hostName = RegistryUtils.getPublishAddress();
        } else {
            hostName = RegistryUtils.getPublishHostName();
        }
        getMicroserviceInstance().setHostName(hostName);

        // Ignore this in the local development mode
        if (RegistryClientFactory.getLocalModeFile().isEmpty()) {
            getMicroserviceInstance().getHealthCheck().setInterval(serviceRegistryConfig.getHeartbeatInterval());
            getMicroserviceInstance().getHealthCheck().setTimes(serviceRegistryConfig.getResendHeartBeatTimes());
        }

        String instanceId = srClient.registerMicroserviceInstance(getMicroserviceInstance());
        if (isEmpty(instanceId)) {
            LOGGER.error("Register microservice instance failed. microserviceId={}",
                    getMicroservice().getServiceId());
            return false;
        }
        getMicroserviceInstance().setInstanceId(instanceId);
        if (RegistryClientFactory.getLocalModeFile().isEmpty()) {
            LOGGER.info(
                    "Register microservice instance success. microserviceId={} instanceId={} endpoints={} lease {}s",
                    microservice.getServiceId(),
                    instanceId,
                    microserviceInstance.getEndpoints(),
                    microserviceInstance.getHealthCheck().getTTL());
        } else {
            LOGGER.info("Register microservice instance success. microserviceId={} instanceId={}",
                    getMicroservice().getServiceId(),
                    instanceId);
        }

        return true;
    }

    public static boolean unregsiterInstance() {
        boolean result = srClient.unregisterMicroserviceInstance(getMicroservice().getServiceId(),
                getMicroserviceInstance().getInstanceId());
        if (!result) {
            LOGGER.error("Unregister microservice instance failed. microserviceId={} instanceId={}",
                    getMicroservice().getServiceId(),
                    getMicroserviceInstance().getInstanceId());
            return false;
        }
        LOGGER.info("Unregister microservice instance success. microserviceId={} instanceId={}",
                getMicroservice().getServiceId(),
                getMicroserviceInstance().getInstanceId());
        return true;
    }

    public static void ensureRegisterMicroservice() throws TimerException {
        // 确保注册成功
        Timer timer = Timer.newForeverTimer();
        while (true) {
            if (regsiterMicroservice()) {
                break;
            }

            timer.sleep();
        }
    }

    private static boolean registerSchemas(String serviceid) {
        for (Entry<String, String> entry : microservice.getSchemaMap().entrySet()) {
            String schemaId = entry.getKey();
            String content = entry.getValue();

            boolean exists = srClient.isSchemaExist(serviceid, schemaId);
            LOGGER.info("schemaId {} exists {}", schemaId, exists);
            if (!exists) {
                if (!srClient.registerSchema(serviceid, schemaId, content)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean regsiterMicroservice() {
        String serviceId = srClient.getMicroserviceId(getMicroservice().getAppId(),
                getMicroservice().getServiceName(),
                getMicroservice().getVersion());
        if (!isEmpty(serviceId)) {
            // 已经注册过了，不需要重新注册
            LOGGER.info(
                    "Microservice exists in service center, no need to register. id={} appId={}, name={}, version={}",
                    serviceId,
                    getMicroservice().getAppId(),
                    getMicroservice().getServiceName(),
                    getMicroservice().getVersion());

            checkSchemaIdSet(serviceId);
        } else {
            serviceId = srClient.registerMicroservice(getMicroservice());
            if (isEmpty(serviceId)) {
                LOGGER.error(
                        "Registry microservice failed. appId={}, name={}, version={}",
                        getMicroservice().getAppId(),
                        getMicroservice().getServiceName(),
                        getMicroservice().getVersion());
                return false;
            }
            LOGGER.info(
                    "Registry Microservice successfully. id={} appId={}, name={}, version={}, schemaIds={}",
                    serviceId,
                    getMicroservice().getAppId(),
                    getMicroservice().getServiceName(),
                    getMicroservice().getVersion(),
                    getMicroservice().getSchemas());

            // 重新注册服务场景下，instanceId不应该缓存
            getMicroserviceInstance().setInstanceId(null);
        }

        registerSchemas(serviceId);

        getMicroservice().setServiceId(serviceId);
        getMicroserviceInstance().setServiceId(getMicroservice().getServiceId());
        return true;
    }

    private static void checkSchemaIdSet(String serviceId) {
        Microservice existMicroservice = srClient.getMicroservice(serviceId);
        Set<String> existSchemas = new HashSet<>(existMicroservice.getSchemas());
        Set<String> localSchemas = new HashSet<>(microservice.getSchemas());
        if (!existSchemas.equals(localSchemas)) {
            LOGGER.error(
                    "SchemaIds is different between local and service center. Please change microservice version. "
                            + "id={} appId={}, name={}, version={}, local schemaIds={}, service center schemaIds={}",
                    serviceId,
                    getMicroservice().getAppId(),
                    getMicroservice().getServiceName(),
                    getMicroservice().getVersion(),
                    localSchemas,
                    existSchemas);
            return;
        }

        LOGGER.info(
                "SchemaIds is equals to service center. id={} appId={}, name={}, version={}, schemaIds={}",
                serviceId,
                getMicroservice().getAppId(),
                getMicroservice().getServiceName(),
                getMicroservice().getVersion(),
                localSchemas);
    }

    public static HeartbeatResponse heartbeat() {
        Timer timer = Timer.newForeverTimer();

        HeartbeatResponse response;
        while (true) {
            response = srClient.heartbeat(getMicroservice().getServiceId(),
                    getMicroserviceInstance().getInstanceId());
            if (response != null) {
                break;
            }
            if (!needToWatch()) {
                exception(new ClientException("could not connect to service center"));
            }

            try {
                LOGGER.warn("Update heartbeat to service center failed, retry after {}s. service={}/{}",
                        timer.getNextTimeout(),
                        getMicroservice().getServiceId(),
                        getMicroserviceInstance().getInstanceId());
                timer.sleep();
            } catch (TimerException e) {
                LOGGER.error(
                        "Update heartbeat to service center failed, can not connect to service center. service={}/{}",
                        getMicroservice().getServiceId(),
                        getMicroserviceInstance().getInstanceId());
                return null;
            }
        }
        if (!response.isOk()) {
            LOGGER.error("Update heartbeat to service center failed, microservice instance={}/{} does not exist",
                    getMicroservice().getServiceId(),
                    getMicroserviceInstance().getInstanceId());
        }
        return response;
    }

    public static List<MicroserviceInstance> findServiceInstance(String appId, String serviceName,
            String versionRule) {
        List<MicroserviceInstance> instances = srClient.findServiceInstance(getMicroservice().getServiceId(),
                appId,
                serviceName,
                versionRule);
        if (instances == null) {
            LOGGER.error("find empty instances from service center. service={}/{}", appId, serviceName);
            return null;
        }

        LOGGER.info("find instances[{}] from service center success. service={}/{}",
                instances.size(),
                appId,
                serviceName);
        for (MicroserviceInstance instance : instances) {
            LOGGER.info("service id={}, instance id={}, endpoints={}",
                    instance.getServiceId(),
                    instance.getInstanceId(),
                    instance.getEndpoints());
        }
        return instances;
    }

    public static void watch() {
        if (!needToWatch()) {
            return;
        }

        srClient.watch(getMicroservice().getServiceId(),
                (event) -> {
                    if (event.failed()) {
                        exception(event.cause());
                        return;
                    }
                    MicroserviceInstanceChangedEvent changedEvent = event.result();
                    if (isProviderInstancesChanged(changedEvent) && !serviceRegistryConfig.isWatch()) {
                        return;
                    }
                    if (!isProviderInstancesChanged(changedEvent)
                            && !serviceRegistryConfig.isRegistryAutoDiscovery()) {
                        return;
                    }
                    InstanceCacheManager.INSTANCE.onInstanceUpdate(changedEvent);
                    InstanceVersionCacheManager.INSTANCE.onInstanceUpdate(changedEvent);
                },
                open -> {
                    recover();
                },
                close -> {
                });
    }

    private static void recover() {
        if (isUnavailabe) {
            isUnavailabe = false;
            NotifyManager.INSTANCE.notify(RegistryEvent.RECOVERED, null);
        }
    }

    public static void exception(Throwable e) {
        if (!isUnavailabe) {
            isUnavailabe = true;
            NotifyManager.INSTANCE.notify(RegistryEvent.EXCEPTION, e);
        }
    }

    private static boolean needToWatch() {
        return serviceRegistryConfig.isWatch() || serviceRegistryConfig.isRegistryAutoDiscovery();
    }

    private static boolean isProviderInstancesChanged(MicroserviceInstanceChangedEvent changedEvent) {
        return !Const.REGISTRY_APP_ID.equals(changedEvent.getKey().getAppId())
                && !Const.REGISTRY_SERVICE_NAME.equals(changedEvent.getKey().getServiceName());
    }

    public static void addListener(RegistryListener listener) {
        NotifyManager.INSTANCE.addListener(listener);
    }

    public static void destory() {
        if (registryThread != null) {
            registryThread.shutdown();
        }
        unregsiterInstance();
    }

    public static String getPublishAddress() {
        String publicAddressSetting =
            DynamicPropertyFactory.getInstance().getStringProperty(PUBLISH_ADDRESS, "").get();
        publicAddressSetting = publicAddressSetting.trim();
        if (publicAddressSetting.isEmpty()) {
            return NetUtils.getHostAddress();
        }

        // placeholder is network interface name
        if (publicAddressSetting.startsWith("{") && publicAddressSetting.endsWith("}")) {
            return NetUtils
                    .ensureGetInterfaceAddress(publicAddressSetting.substring(1, publicAddressSetting.length() - 1))
                    .getHostAddress();
        }

        return publicAddressSetting;
    }

    public static String getPublishHostName() {
        String publicAddressSetting =
            DynamicPropertyFactory.getInstance().getStringProperty(PUBLISH_ADDRESS, "").get();
        publicAddressSetting = publicAddressSetting.trim();
        if (publicAddressSetting.isEmpty()) {
            return NetUtils.getHostName();
        }

        if (publicAddressSetting.startsWith("{") && publicAddressSetting.endsWith("}")) {
            return NetUtils
                    .ensureGetInterfaceAddress(publicAddressSetting.substring(1, publicAddressSetting.length() - 1))
                    .getHostName();
        }

        return publicAddressSetting;
    }

    /**
     * 对于配置为0.0.0.0的地址，通过查询网卡地址，转换为实际监听的地址。
     */
    public static String getPublishAddress(String schema, String address) {
        if (address == null) {
            return address;
        }

        try {
            String publicAddressSetting = DynamicPropertyFactory.getInstance()
                    .getStringProperty(PUBLISH_ADDRESS, "")
                    .get();
            publicAddressSetting = publicAddressSetting.trim();

            URI originalURI = new URI(schema + "://" + address);
            IpPort ipPort = NetUtils.parseIpPort(originalURI.getAuthority());
            if (ipPort == null) {
                LOGGER.warn("address {} not valid.", address);
                return null;
            }

            InetSocketAddress socketAddress = ipPort.getSocketAddress();
            String host = socketAddress.getAddress().getHostAddress();

            if (publicAddressSetting.isEmpty()) {
                if (socketAddress.getAddress().isAnyLocalAddress()) {
                    host = NetUtils.getHostAddress();
                    LOGGER.warn("address {}, auto select a host address to publish {}:{}, maybe not the correct one",
                            address,
                            host,
                            socketAddress.getPort());
                    URI newURI = new URI(originalURI.getScheme(), originalURI.getUserInfo(), host,
                            originalURI.getPort(), originalURI.getPath(), originalURI.getQuery(),
                            originalURI.getFragment());
                    return newURI.toString();
                } else {
                    return originalURI.toString();
                }
            }

            if (publicAddressSetting.startsWith("{") && publicAddressSetting.endsWith("}")) {
                publicAddressSetting = NetUtils
                        .ensureGetInterfaceAddress(
                                publicAddressSetting.substring(1, publicAddressSetting.length() - 1))
                        .getHostAddress();
            }

            String publishPortKey = PUBLISH_PORT.replace("{transport_name}", originalURI.getScheme());
            int publishPortSetting = DynamicPropertyFactory.getInstance()
                    .getIntProperty(publishPortKey, 0)
                    .get();
            int publishPort = publishPortSetting == 0 ? originalURI.getPort() : publishPortSetting;
            URI newURI = new URI(originalURI.getScheme(), originalURI.getUserInfo(), publicAddressSetting,
                    publishPort, originalURI.getPath(), originalURI.getQuery(), originalURI.getFragment());
            return newURI.toString();

        } catch (URISyntaxException e) {
            LOGGER.warn("address {} not valid.", address);
            return null;
        }
    }

    /**
     * 更新本实例的properties
     */
    public static boolean updateInstanceProperties(Map<String, String> instanceProperties) {
        boolean success = srClient.updateInstanceProperties(microserviceInstance.getServiceId(),
                microserviceInstance.getInstanceId(),
                instanceProperties);
        if (success) {
            microserviceInstance.setProperties(instanceProperties);
        }
        return success;
    }

    public static Microservice getMicroservice(String microserviceId) {
        return srClient.getMicroservice(microserviceId);
    }

}
