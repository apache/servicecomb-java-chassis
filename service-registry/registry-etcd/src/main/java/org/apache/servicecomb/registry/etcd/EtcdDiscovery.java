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
package org.apache.servicecomb.registry.etcd;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.registry.api.Discovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.google.common.collect.Lists;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;

public class EtcdDiscovery implements Discovery<EtcdDiscoveryInstance> {

  private Environment environment;

  private String basePath;

  private EtcdRegistryProperties etcdRegistryProperties;

  private Client client;

  private InstanceChangedListener<EtcdDiscoveryInstance> instanceChangedListener;

  private static volatile boolean isWatch = false;

  private static final Logger LOGGER = LoggerFactory.getLogger(EtcdDiscovery.class);

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

    String prefixPath = basePath + "/" + application + "/" + serviceName;
    SingletonManager.getInstance().getSingleton(prefixPath, serName -> {
      Watch watchClient = client.getWatchClient();
      try {
        watchClient.watch(ByteSequence.from(prefixPath, Charset.defaultCharset()), WatchOption.builder().build(),
            resp -> {
              List<KeyValue> keyValueList = resp.getEvents().stream().map(WatchEvent::getKeyValue).toList();
              instanceChangedListener.onInstanceChanged(name(), application, serviceName,
                  convertServiceInstanceList(keyValueList));
            });
      } catch (Exception e) {
        LOGGER.error("add watch failure", e);
      }
      return watchClient;
    });
    List<KeyValue> endpointKv = getValuesByPrefix(prefixPath);
    return convertServiceInstanceList(endpointKv);
  }

  public List<KeyValue> getValuesByPrefix(String prefix) {

    CompletableFuture<GetResponse> getFuture = client.getKVClient()
        .get(ByteSequence.from(prefix, StandardCharsets.UTF_8),
            GetOption.builder().withPrefix(ByteSequence.from(prefix, StandardCharsets.UTF_8)).build());
    GetResponse response = MuteExceptionUtil.builder().withLog("get kv by prefix error")
        .executeCompletableFuture(getFuture);
    return response.getKvs();
  }

  private List<EtcdDiscoveryInstance> convertServiceInstanceList(List<KeyValue> keyValueList) {

    List<EtcdDiscoveryInstance> list = Lists.newArrayListWithExpectedSize(keyValueList.size());
    for (KeyValue keyValue : keyValueList) {
      String valueJson = new String(keyValue.getValue().getBytes(), Charset.defaultCharset());

      EtcdInstance etcdInstance = MuteExceptionUtil.builder()
          .withLog("convert json value to obj from etcd failure, {}", valueJson)
          .executeFunctionWithDoubleParam(JsonUtils::readValue, valueJson.getBytes(StandardCharsets.UTF_8),
              EtcdInstance.class);
      EtcdDiscoveryInstance etcdDiscoveryInstance = new EtcdDiscoveryInstance(etcdInstance);
      list.add(etcdDiscoveryInstance);
    }
    return list;
  }

  @Override
  public List<String> findServices(String application) {

    String prefixPath = basePath + "/" + application;
    List<KeyValue> endpointKv = getValuesByPrefix(prefixPath);
    return endpointKv.stream().map(kv -> kv.getKey().toString(StandardCharsets.UTF_8)) // 转换 ByteSequence 为 String
        .collect(Collectors.toList());
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
    client = Client.builder().endpoints(etcdRegistryProperties.getConnectString()).build();
  }

  @Override
  public void destroy() {
    if (client != null) {
      client.close();
    }
  }
}
