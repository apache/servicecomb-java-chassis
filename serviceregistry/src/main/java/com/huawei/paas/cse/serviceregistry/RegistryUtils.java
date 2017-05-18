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

package com.huawei.paas.cse.serviceregistry;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.paas.cse.serviceregistry.api.Const;
import com.huawei.paas.cse.serviceregistry.api.registry.BasePath;
import com.huawei.paas.cse.serviceregistry.api.registry.HealthCheck;
import com.huawei.paas.cse.serviceregistry.api.registry.HealthCheckMode;
import com.huawei.paas.cse.serviceregistry.api.registry.Microservice;
import com.huawei.paas.cse.serviceregistry.api.registry.MicroserviceInstance;
import com.huawei.paas.cse.serviceregistry.api.response.HeartbeatResponse;
import com.huawei.paas.cse.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import com.huawei.paas.cse.serviceregistry.cache.CacheRegistryListener;
import com.huawei.paas.cse.serviceregistry.cache.InstanceCacheManager;
import com.huawei.paas.cse.serviceregistry.client.ClientException;
import com.huawei.paas.cse.serviceregistry.client.RegistryClientFactory;
import com.huawei.paas.cse.serviceregistry.client.ServiceRegistryClient;
import com.huawei.paas.cse.serviceregistry.config.InstancePropertiesLoader;
import com.huawei.paas.cse.serviceregistry.config.MicroservicePropertiesLoader;
import com.huawei.paas.cse.serviceregistry.config.ServiceRegistryConfig;
import com.huawei.paas.cse.serviceregistry.notify.NotifyManager;
import com.huawei.paas.cse.serviceregistry.notify.NotifyThread;
import com.huawei.paas.cse.serviceregistry.notify.RegistryEvent;
import com.huawei.paas.cse.serviceregistry.notify.RegistryListener;
import com.huawei.paas.cse.serviceregistry.utils.Timer;
import com.huawei.paas.cse.serviceregistry.utils.TimerException;
import com.huawei.paas.foundation.common.CommonThread;
import com.huawei.paas.foundation.common.net.IpPort;
import com.huawei.paas.foundation.common.net.NetUtils;
import com.netflix.config.DynamicPropertyFactory;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author
 * @version [版本号, 2016年12月8日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
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

    private static final String PUBLISH_ADDRESS = "cse.service.publishAddress";

    private static CommonThread registryThread = null;

    private static boolean isUnavailabe = false;

    private RegistryUtils() {
    }

    /**
     * 获取microservice的值
     * 
     * @return 返回 microservice
     */
    public static Microservice getMicroservice() {
        if (microservice == null) {
            microservice = createMicroserviceFromDefinition();
        }

        return microservice;
    }

    /**
     * 获取microserviceInstance的值
     * 
     * @return 返回 microserviceInstance
     */
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
        microservice
                .setAppId(DynamicPropertyFactory.getInstance().getStringProperty("APPLICATION_ID", "default").get());
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

        // This is not the local mode for the development so we need to set the
        // healthcheck
        if (RegistryClientFactory.getLocalModeFile().isEmpty()) {
            HealthCheck healthCheck = new HealthCheck();
            healthCheck.setMode(HealthCheckMode.HEARTBEAT);
            microserviceInstance.setHealthCheck(healthCheck);
        }

        return microserviceInstance;
    }

    /**
     * This method is written so that the UT of this class can be done
     * appropriately.
     */
    public static void setSrClient(ServiceRegistryClient oServiceRegistryClient) {
        srClient = oServiceRegistryClient;
    }

    /**
     * <一句话功能简述> <功能详细描述>
     * 
     * @throws Exception
     */
    public static void init() throws Exception {
        serviceRegistryConfig = ServiceRegistryConfig.INSTANCE;
        if (null == srClient) {
            srClient = RegistryClientFactory.getRegistryClient(); // Changes has
                                                                  // been done
                                                                  // to test the
                                                                  // code
                                                                  // properly
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

    /**
     * <一句话功能简述> <功能详细描述>
     */
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

    /**
     * <一句话功能简述> <功能详细描述>
     * 
     * @return
     */
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
            LOGGER.error("Register microservice instance failed. microserviceId={}", getMicroservice().getServiceId());
            return false;
        }
        getMicroserviceInstance().setInstanceId(instanceId);
        if (RegistryClientFactory.getLocalModeFile().isEmpty()) {
            LOGGER.info(
                    "Register microservice instance success. microserviceId={} instanceId={} endpoints={} lease {}s",
                    microservice.getServiceId(), instanceId, microserviceInstance.getEndpoints(),
                    microserviceInstance.getHealthCheck().getTTL());
        } else {
            LOGGER.info("Register microservice instance success. microserviceId={} instanceId={}",
                    getMicroservice().getServiceId(), instanceId);
        }

        return true;
    }

    /**
     * <一句话功能简述> <功能详细描述>
     * 
     * @return
     */
    public static boolean unregsiterInstance() {
        boolean result = srClient.unregisterMicroserviceInstance(getMicroservice().getServiceId(),
                getMicroserviceInstance().getInstanceId());
        if (!result) {
            LOGGER.error("Unregister microservice instance failed. microserviceId={} instanceId={}",
                    getMicroservice().getServiceId(), getMicroserviceInstance().getInstanceId());
            return false;
        }
        LOGGER.info("Unregister microservice instance success. microserviceId={} instanceId={}",
                getMicroservice().getServiceId(), getMicroserviceInstance().getInstanceId());
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
        String serviceId = srClient.getMicroserviceId(getMicroservice().getAppId(), getMicroservice().getServiceName(),
                getMicroservice().getVersion());
        if (!isEmpty(serviceId)) {
            // 已经注册过了，不需要重新注册
            LOGGER.info(
                    "Microservice exists in service center, no need to register. id={} appId={}, name={}, version={}",
                    serviceId, getMicroservice().getAppId(), getMicroservice().getServiceName(),
                    getMicroservice().getVersion());
        } else {
            serviceId = srClient.registerMicroservice(getMicroservice());
            if (isEmpty(serviceId)) {
                LOGGER.error("Registry microservice failed. appId={}, name={}, version={}",
                        getMicroservice().getAppId(), getMicroservice().getServiceName(),
                        getMicroservice().getVersion());
                return false;
            }
            LOGGER.info("Registry Microservice successfully. id={} appId={}, name={}, version={}", serviceId,
                    getMicroservice().getAppId(), getMicroservice().getServiceName(), getMicroservice().getVersion());
        }

        registerSchemas(serviceId);

        getMicroservice().setServiceId(serviceId);
        getMicroserviceInstance().setServiceId(getMicroservice().getServiceId());
        return true;
    }

    public static HeartbeatResponse heartbeat() {
        Timer timer = Timer.newForeverTimer();

        HeartbeatResponse response;
        while (true) {
            response = srClient.heartbeat(getMicroservice().getServiceId(), getMicroserviceInstance().getInstanceId());
            if (response != null) {
                break;
            }
            if (!needToWatch()) {
                exception(new ClientException("could not connect to service center"));
            }

            try {
                LOGGER.warn("Update heartbeat to service center failed, retry after {}s. service={}/{}",
                        timer.getNextTimeout(), getMicroservice().getServiceId(),
                        getMicroserviceInstance().getInstanceId());
                timer.sleep();
            } catch (TimerException e) {
                LOGGER.error(
                        "Update heartbeat to service center failed, can not connect to service center. service={}/{}",
                        getMicroservice().getServiceId(), getMicroserviceInstance().getInstanceId());
                return null;
            }
        }
        if (!response.isOk()) {
            LOGGER.error("Update heartbeat to service center failed, microservice instance={}/{} does not exist",
                    getMicroservice().getServiceId(), getMicroserviceInstance().getInstanceId());
        }
        return response;
    }

    public static List<MicroserviceInstance> findServiceInstance(String appId, String serviceName, String versionRule) {
        List<MicroserviceInstance> instances = srClient.findServiceInstance(getMicroservice().getServiceId(), appId,
                serviceName, versionRule);
        if (instances == null) {
            LOGGER.error("find empty instances from service center. service={}/{}", appId, serviceName);
            return null;
        }

        LOGGER.info("find instances[{}] from service center success. service={}/{}", instances.size(), appId,
                serviceName);
        return instances;
    }

    public static void watch() {
        if (!needToWatch()) {
            return;
        }

        srClient.watch(getMicroservice().getServiceId(), (event) -> {
            if (event.failed()) {
                exception(event.cause());
                return;
            }
            MicroserviceInstanceChangedEvent changedEvent = event.result();
            if (isProviderInstancesChanged(changedEvent) && !serviceRegistryConfig.isWatch()) {
                return;
            }
            if (!isProviderInstancesChanged(changedEvent) && !serviceRegistryConfig.isRegistryAutoDiscovery()) {
                return;
            }
            InstanceCacheManager.INSTANCE.onInstanceUpdate(changedEvent);
        }, open -> {
            recover();
        }, close -> {
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
        String publicAddressSetting = DynamicPropertyFactory.getInstance().getStringProperty(PUBLISH_ADDRESS, "").get();
        publicAddressSetting = publicAddressSetting.trim();
        // String publicAddressSetting = System.getProperty(PUBLISH_ADDRESS);
        if (publicAddressSetting.isEmpty()) {
            return NetUtils.getHostAddress();
        } else {
            if (publicAddressSetting.startsWith("{") && publicAddressSetting.endsWith("}")) {
                return NetUtils.getHostAddress(publicAddressSetting.substring(1, publicAddressSetting.length() - 1));
            } else {
                return publicAddressSetting;
            }
        }
    }

    public static String getPublishHostName() {
        String publicAddressSetting = DynamicPropertyFactory.getInstance().getStringProperty(PUBLISH_ADDRESS, "").get();
        publicAddressSetting = publicAddressSetting.trim();
        if (publicAddressSetting.isEmpty()) {
            return NetUtils.getHostName();
        } else {
            if (publicAddressSetting.startsWith("{") && publicAddressSetting.endsWith("}")) {
                return publicAddressSetting.substring(1, publicAddressSetting.length() - 1);
            } else {
                return publicAddressSetting;
            }
        }
    }

    /**
     * 对于配置为0.0.0.0的地址，通过查询网卡地址，转换为实际监听的地址。
     * 
     * @param schema
     *            schema, e.g. http
     * @param address
     *            adddress, e.g 0.0.0.0:8080
     * @return 实际监听的地址
     */
    public static String getPublishAddress(String schema, String address) {
        if (address == null) {
            return address;
        }

        try {
            String publicAddressSetting = DynamicPropertyFactory.getInstance().getStringProperty(PUBLISH_ADDRESS, "")
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
                            address, host, socketAddress.getPort());
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
                        .getHostAddress(publicAddressSetting.substring(1, publicAddressSetting.length() - 1));
            }
            URI newURI = new URI(originalURI.getScheme(), originalURI.getUserInfo(), publicAddressSetting,
                    originalURI.getPort(), originalURI.getPath(), originalURI.getQuery(), originalURI.getFragment());
            return newURI.toString();

        } catch (URISyntaxException e) {
            LOGGER.warn("address {} not valid.", address);
            return null;
        }
    }

    /**
     * 更新本实例的properties
     * 
     * @param microserviceId
     * @param microserviceInstanceId
     * @param instanceProperties
     * @return
     */
    public static boolean updateInstanceProperties(Map<String, String> instanceProperties) {
        boolean success = srClient.updateInstanceProperties(microserviceInstance.getServiceId(),
                microserviceInstance.getInstanceId(), instanceProperties);
        if (success) {
            microserviceInstance.setProperties(instanceProperties);
        }
        return success;
    }

}
