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
package org.apache.servicecomb.config.etcd;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.etcd.EtcdDynamicPropertiesSource.UpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;

public class EtcdClient {

  public class GetDataRunable implements Runnable {

    private Map<String, Object> dataMap;

    private EtcdClient etcdClient;

    private String path;

    public GetDataRunable(Map<String, Object> dataMap, EtcdClient etcdClient, String path) {
      this.dataMap = dataMap;
      this.etcdClient = etcdClient;
      this.path = path;
    }

    @Override
    public void run() {
      try {
        dataMap.clear();
        dataMap.putAll(etcdClient.parseData(path));
        refreshConfigItems();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(EtcdClient.class);

  public static final String PATH_ENVIRONMENT = "/servicecomb/config/environment/%s";

  public static final String PATH_APPLICATION = "/servicecomb/config/application/%s/%s";

  public static final String PATH_SERVICE = "/servicecomb/config/service/%s/%s/%s";

  public static final String PATH_VERSION = "/servicecomb/config/version/%s/%s/%s/%s";

  public static final String PATH_TAG = "/servicecomb/config/tag/%s/%s/%s/%s/%s";

  private final UpdateHandler updateHandler;

  private final EtcdConfig etcdConfig;

  private final Environment environment;

  private final Object lock = new Object();

  private Map<String, Object> environmentData = new HashMap<>();

  private Map<String, Object> applicationData = new HashMap<>();

  private Map<String, Object> serviceData = new HashMap<>();

  private Map<String, Object> versionData = new HashMap<>();

  private Map<String, Object> tagData = new HashMap<>();

  private Map<String, Object> allLast = new HashMap<>();

  private Client client;

  public EtcdClient(UpdateHandler updateHandler, Environment environment) {
    this.updateHandler = updateHandler;
    this.etcdConfig = new EtcdConfig(environment);
    this.environment = environment;
  }

  public void getClient() {
    if (StringUtils.isEmpty(etcdConfig.getAuthInfo())) {
      this.client = Client.builder().endpoints(etcdConfig.getConnectString()).build();
    } else {
      String[] authInfo = etcdConfig.getAuthInfo().split(":");
      this.client = Client.builder().endpoints(etcdConfig.getConnectString())
          .user(ByteSequence.from(authInfo[0], Charset.defaultCharset()))
          .password(ByteSequence.from(authInfo[1], Charset.defaultCharset())).build();
    }
  }

  public void refreshEtcdConfig() throws Exception {

    getClient();
    String env = BootStrapProperties.readServiceEnvironment(environment);
    if (StringUtils.isEmpty(env)) {
      env = EtcdConfig.ZOOKEEPER_DEFAULT_ENVIRONMENT;
    }
    addEnvironmentConfig(env);
    addApplicationConfig(env);
    addServiceConfig(env);
    addVersionConfig(env);
    addTagConfig(env);

    refreshConfigItems();
  }

  private void addTagConfig(String env) throws Exception {
    if (StringUtils.isEmpty(etcdConfig.getInstanceTag())) {
      return;
    }
    String path = String.format(PATH_TAG, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment),
        BootStrapProperties.readServiceVersion(environment),
        etcdConfig.getInstanceTag());

    ByteSequence prefixByteSeq = ByteSequence.from(path, Charset.defaultCharset());
    Watch watchClient = client.getWatchClient();
    watchClient.watch(prefixByteSeq, WatchOption.builder().withPrefix(prefixByteSeq).build(),
        resp -> new Thread(new GetDataRunable(tagData, this, path)).start());
    this.tagData = parseData(path);
  }

  private void addVersionConfig(String env) throws Exception {
    String path = String.format(PATH_VERSION, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment),
        BootStrapProperties.readServiceVersion(environment));

    ByteSequence prefixByteSeq = ByteSequence.from(path, Charset.defaultCharset());
    Watch watchClient = client.getWatchClient();
    watchClient.watch(prefixByteSeq, WatchOption.builder().withPrefix(prefixByteSeq).build(),
        resp -> new Thread(new GetDataRunable(versionData, this, path)).start());
    this.versionData = parseData(path);
  }

  private void addServiceConfig(String env) throws Exception {
    String path = String.format(PATH_SERVICE, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment));

    ByteSequence prefixByteSeq = ByteSequence.from(path, Charset.defaultCharset());
    Watch watchClient = client.getWatchClient();
    watchClient.watch(prefixByteSeq, WatchOption.builder().withPrefix(prefixByteSeq).build(),
        resp -> new Thread(new GetDataRunable(serviceData, this, path)).start());
    this.serviceData = parseData(path);
  }

  private void addApplicationConfig(String env) throws Exception {
    String path = String.format(PATH_APPLICATION, env, BootStrapProperties.readApplication(environment));

    ByteSequence prefixByteSeq = ByteSequence.from(path, Charset.defaultCharset());
    Watch watchClient = client.getWatchClient();
    watchClient.watch(prefixByteSeq, WatchOption.builder().withPrefix(prefixByteSeq).build(),
        resp -> new Thread(new GetDataRunable(applicationData, this, path)).start());
    this.applicationData = parseData(path);
  }

  private void addEnvironmentConfig(String env) throws Exception {
    String path = String.format(PATH_ENVIRONMENT, env);

    ByteSequence prefixByteSeq = ByteSequence.from(path, Charset.defaultCharset());
    Watch watchClient = client.getWatchClient();
    watchClient.watch(prefixByteSeq, WatchOption.builder().withPrefix(prefixByteSeq).build(),
        resp -> new Thread(new GetDataRunable(environmentData, this, path)).start());
    this.environmentData = parseData(path);
  }

  public Map<String, Object> parseData(String path) throws Exception {

    List<KeyValue> endpointKv = getValuesByPrefix(path);
    return getValues(path, endpointKv);
  }

  private Map<String, Object> getValues(String path, List<KeyValue> endpointKv) {
    Map<String, Object> values = new HashMap<>();
    for (KeyValue keyValue : endpointKv) {
      String key = new String(keyValue.getKey().getBytes(), StandardCharsets.UTF_8);
      String value = new String(keyValue.getValue().getBytes(), StandardCharsets.UTF_8);
      if (key.equals(path)) {
        continue;
      }
      if (key.endsWith(".yaml") || key.endsWith(".yml")) {
        YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ByteArrayResource(value.getBytes(StandardCharsets.UTF_8)));
        values.putAll(toMap(yamlFactory.getObject()));
      } else if (key.endsWith(".properties")) {
        Properties properties = new Properties();
        try {
          properties.load(new StringReader(value));
        } catch (IOException e) {
          LOGGER.error("load error");
        }
        values.putAll(toMap(properties));
      } else {
        values.put(key, value);
      }
    }
    return values;
  }

  private List<KeyValue> getValuesByPrefix(String prefix) {

    CompletableFuture<GetResponse> getFuture = client.getKVClient()
        .get(ByteSequence.from(prefix, StandardCharsets.UTF_8),
            GetOption.builder().withPrefix(ByteSequence.from(prefix, StandardCharsets.UTF_8)).build());
    GetResponse response = MuteExceptionUtil.builder().withLog("get kv by prefix error")
        .executeCompletableFuture(getFuture);
    return response.getKvs();
  }

  private void refreshConfigItems() {
    synchronized (lock) {
      Map<String, Object> all = new HashMap<>();
      all.putAll(environmentData);
      all.putAll(applicationData);
      all.putAll(serviceData);
      all.putAll(versionData);
      all.putAll(tagData);
      updateHandler.handle(all, allLast);
      this.allLast = all;
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> toMap(Properties properties) {
    if (properties == null) {
      return Collections.emptyMap();
    }
    Map<String, Object> result = new HashMap<>();
    Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      Object value = properties.getProperty(key);
      result.put(key, value);
    }
    return result;
  }
}
