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
package org.apache.servicecomb.registry.zookeeper;

import com.google.common.collect.Lists;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.registry.api.Discovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class EtcdDiscovery implements Discovery<EtcdDiscoveryInstance> {

    private Environment environment;

    private String basePath;

    private EtcdRegistryProperties etcdRegistryProperties;

    private Client client;

    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdDiscovery.class);

    private InstanceChangedListener<EtcdDiscoveryInstance> instanceChangedListener;

    @Autowired
    @SuppressWarnings("unused")
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Autowired
    @SuppressWarnings("unused")
    public void setEtcdRegistryProperties(EtcdRegistryProperties etcdRegistryProperties) {
        this.etcdRegistryProperties = etcdRegistryProperties;
    }

    @Override
    public String name() {
        return EtcdConst.ETCD_REGISTRY_NAME;
    }

    @Override
    public boolean enabled(String application, String serviceName) {
        return environment.getProperty(String.format(EtcdConst.ETCD_DISCOVERY_ENABLED, application, serviceName),
                boolean.class, true);
    }

    @Override
    public List<EtcdDiscoveryInstance> findServiceInstances(String application, String serviceName) {

        KV kvClient = client.getKVClient();
        String path = basePath + "/" + application;
        GetResponse response = MuteExceptionUtil.executeCompletableFuture(
                kvClient.get(ByteSequence.from(path, Charset.defaultCharset()),
                GetOption.builder().build()));
        Watch watchClient = client.getWatchClient();
        watchClient.watch(ByteSequence.from(path, Charset.defaultCharset()),
                WatchOption.newBuilder().build(),
                resp -> {
                    List<KeyValue> keyValueList = resp.getEvents().stream().map(WatchEvent::getKeyValue).toList();
                    instanceChangedListener.onInstanceChanged(name(), application, serviceName, convertServiceInstanceList(keyValueList));
                }
        );

        return convertServiceInstanceList(response.getKvs());
    }

    private List<EtcdDiscoveryInstance> convertServiceInstanceList(List<KeyValue> keyValueList) {

        List<EtcdDiscoveryInstance> list = Lists.newArrayListWithExpectedSize(keyValueList.size());
        for (KeyValue keyValue : keyValueList) {
            String valueJson = new String(keyValue.getValue().getBytes());
            EtcdDiscoveryInstance etcdDiscoveryInstance = JsonUtils.convertValue(valueJson, EtcdDiscoveryInstance.class);
            list.add(etcdDiscoveryInstance);
        }
        return list;
    }

//    todo
    @Override
    public List<String> findServices(String application) {

        return List.of();
    }

    @Override
    public void setInstanceChangedListener(InstanceChangedListener<EtcdDiscoveryInstance> instanceChangedListener) {
        this.instanceChangedListener = instanceChangedListener;
    }

    @Override
    public boolean enabled() {
        return etcdRegistryProperties.isEnabled();
    }

    @Override
    public void init() {
        String env = BootStrapProperties.readServiceEnvironment(environment);
        if (StringUtils.isEmpty(env)) {
            env = EtcdConst.ETCD_DEFAULT_ENVIRONMENT;
        }
        basePath = String.format(EtcdConst.ETCD_DISCOVERY_ROOT, env);
    }

    @Override
    public void run() {
        client = Client.builder().endpoints(etcdRegistryProperties.getConnectString())
                .build();
    }

    @Override
    public void destroy() {
        if (client != null) {
            client.close();
        }
    }
}
