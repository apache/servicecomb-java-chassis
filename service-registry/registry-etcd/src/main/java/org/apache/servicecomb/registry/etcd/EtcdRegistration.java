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

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.options.PutOption;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.DataCenterProperties;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.registry.RegistrationId;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EtcdRegistration implements Registration<EtcdRegistrationInstance> {

  private EtcdInstance etcdInstance;

  private Environment environment;

  private String basePath;

  private RegistrationId registrationId;

  private DataCenterProperties dataCenterProperties;

  private EtcdRegistryProperties etcdRegistryProperties;

  private Client client;

  private ScheduledExecutorService  executorService;

  private String keyPath;

  private Long leaseId;

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

  @Autowired
  public void setRegistrationId(RegistrationId registrationId) {
    this.registrationId = registrationId;
  }

  @Autowired
  @SuppressWarnings("unused")
  public void setDataCenterProperties(DataCenterProperties dataCenterProperties) {
    this.dataCenterProperties = dataCenterProperties;
  }

  @Override
  public String name() {
    return EtcdConst.ETCD_REGISTRY_NAME;
  }

  @Override
  public EtcdRegistrationInstance getMicroserviceInstance() {
    return new EtcdRegistrationInstance(etcdInstance);
  }

  @Override
  public boolean updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status) {
    return true;
  }

  @Override
  public void addSchema(String schemaId, String content) {
    etcdInstance.addSchema(schemaId, content);
  }

  @Override
  public void addEndpoint(String endpoint) {
    etcdInstance.addEndpoint(endpoint);
  }

  @Override
  public void addProperty(String key, String value) {
    etcdInstance.addProperty(key, value);
  }

  @Override
  public boolean enabled() {
    return false;
  }

  @Override
  public void init() {
    String env = BootStrapProperties.readServiceEnvironment(environment);
    if (StringUtils.isEmpty(env)) {
      env = EtcdConst.ETCD_DEFAULT_ENVIRONMENT;
    }
    basePath = String.format(EtcdConst.ETCD_DISCOVERY_ROOT, env);
    etcdInstance = new EtcdInstance();
    etcdInstance.setInstanceId(registrationId.getInstanceId());
    etcdInstance.setEnvironment(env);
    etcdInstance.setApplication(BootStrapProperties.readApplication(environment));
    etcdInstance.setServiceName(BootStrapProperties.readServiceName(environment));
    etcdInstance.setAlias(BootStrapProperties.readServiceAlias(environment));
    etcdInstance.setDescription(BootStrapProperties.readServiceDescription(environment));
    if (StringUtils.isNotEmpty(dataCenterProperties.getName())) {
      DataCenterInfo dataCenterInfo = new DataCenterInfo();
      dataCenterInfo.setName(dataCenterProperties.getName());
      dataCenterInfo.setRegion(dataCenterProperties.getRegion());
      dataCenterInfo.setAvailableZone(dataCenterProperties.getAvailableZone());
      etcdInstance.setDataCenterInfo(dataCenterInfo);
    }
    etcdInstance.setProperties(BootStrapProperties.readServiceProperties(environment));
    etcdInstance.setVersion(BootStrapProperties.readServiceVersion(environment));
  }

  @Override
  public void run() {
    client = Client.builder().endpoints(etcdRegistryProperties.getConnectString())
            .build();
    keyPath = basePath + "/" + BootStrapProperties.readApplication(environment) + "/" + registrationId.getInstanceId();

    String valueJson = MuteExceptionUtil.builder().withLog("to json, key:{}, value:{}", keyPath, etcdInstance)
            .executeFunction(JsonUtils::writeValueAsString, etcdInstance);
    register(ByteSequence.from(keyPath , Charset.defaultCharset()),
            ByteSequence.from(valueJson, Charset.defaultCharset()));
  }

  public void register(ByteSequence key, ByteSequence value) {

    Lease leaseClient = client.getLeaseClient();
    leaseId = MuteExceptionUtil.builder().withLog("get lease id, key:{}, value:{}", keyPath, etcdInstance)
            .executeCompletableFuture(leaseClient.grant(60)).getID();
    KV kvClient = client.getKVClient();

    PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
    CompletableFuture<PutResponse> putResponse = kvClient.put(key, value, putOption);
    putResponse.thenRun(() -> {
      executorService = Executors.newSingleThreadScheduledExecutor();
      executorService.scheduleAtFixedRate(() -> {
        MuteExceptionUtil.builder().withLog("reRegister, {}, {}", keyPath, etcdInstance)
                .executeFunction(leaseClient::keepAliveOnce, leaseId);
      }, 0, 5, TimeUnit.SECONDS);
    });
  }

  public void unregister() {
    // 关闭定时任务
    executorService.shutdownNow();
    // 撤销租约，自动删除临时节点
    Lease leaseClient = client.getLeaseClient();
    leaseClient.revoke(leaseId);
    client.getKVClient().delete(ByteSequence.from(keyPath , Charset.defaultCharset()));
  }

  @Override
  public void destroy() {
    if (client != null) {
      unregister();
      client.close();
    }
  }
}