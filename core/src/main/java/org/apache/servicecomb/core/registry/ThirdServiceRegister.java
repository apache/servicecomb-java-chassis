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
package org.apache.servicecomb.core.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.invocation.endpoint.EndpointUtils;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.consumer.MicroserviceVersions;
import org.apache.servicecomb.registry.consumer.StaticMicroserviceVersions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

/**
 * <pre>
 * for declare a 3rd service as a servicecomb service
 * assume a 3rd service:
 *   1. named svc
 *   2. have 2 address: https://svc-1 and https://svc-2
 *   3. have 2 schemas: schema1 and schema2
 *
 * usage:
 *   1. define schema interface in JAX-RS or springMVC mode
 *     1) schema1
 *      {@code
 *        @Path("/v1/svc")
 *        public interface Schema1Client {
 *          @GET
 *          @Path("/add")
 *          int add(@QueryParam("q-x") int x, @QueryParam("q-y") int y);
 *
 *          @GET
 *          @Path("/minus")
 *          int minus(@QueryParam("q-x") int x, @QueryParam("q-y") int y);
 *        }
 *      }
 *     2) schema2
 *   2. add configuration to microservice.yaml
 *      {@code
 *        svc:
 *          urls:
 *            - https://svc-1
 *            - https://svc-2
 *      }
 *   3. declare the 3rd service
 *      {@code
 *        @Configuration
 *        public class Svc extends ThirdServiceRegister {
 *          public static final String NAME = "svc";
 *
 *          public Svc() {
 *            super(NAME);
 *
 *            addSchema("schema1", Schema1Client.class);
 *            addSchema("schema2", Schema2Client.class);
 *          }
 *        }
 *      }
 * </pre>
 */
public abstract class ThirdServiceRegister implements BootListener, EnvironmentAware {
  private static final Logger LOGGER = LoggerFactory.getLogger(ThirdServiceRegister.class);

  public static final int ORDER = -1000;

  public static final String VERSION = "1.0";

  public static final List<String> DEFAULT_ADDRESSES = Collections.singletonList("http://127.0.0.1");

  /**
   * role: {@link EndpointUtils}<br>
   * default to any address
   */
  protected List<String> urls = DEFAULT_ADDRESSES;

  protected String appId;

  protected final String microserviceName;

  // for 3rd service, schema interface is client interface too
  protected final Map<String, Class<?>> schemaByIdMap = new HashMap<>();

  public ThirdServiceRegister(String microserviceName) {
    this.microserviceName = microserviceName;
  }

  @Override
  public int getOrder() {
    return ORDER;
  }

  public List<String> getUrls() {
    return urls;
  }

  @Override
  public void setEnvironment(Environment environment) {
    String urlKey = microserviceName + ".urls";
    @SuppressWarnings("unchecked")
    List<String> urls = environment.getProperty(urlKey, List.class);
    setUrls(urlKey, urls);
  }

  public void setUrls(String urlKey, List<String> urls) {
    if (CollectionUtils.isEmpty(urls)) {
      LOGGER.warn("missing configuration, key = {}", urlKey);
      return;
    }

    this.urls = urls;
  }

  protected void addSchema(String schemaId, Class<?> schemaCls) {
    schemaByIdMap.put(schemaId, schemaCls);
  }

  @Override
  public void onBeforeRegistry(BootEvent event) {
    appId = event.getScbEngine().getAppId();
    registerMicroserviceMapping();
  }

  protected void registerMicroserviceMapping() {
    List<String> endpoints = createEndpoints();
    List<MicroserviceInstance> instances = createInstances(endpoints);

    DiscoveryManager.INSTANCE.getAppManager()
        .getOrCreateMicroserviceManager(appId)
        .getVersionsByName()
        .computeIfAbsent(microserviceName, svcName -> createMicroserviceVersions(instances));

    LOGGER.info("register third service, name={}, endpoints={}.", microserviceName, endpoints);
  }

  protected List<String> createEndpoints() {
    return urls.stream()
        .map(EndpointUtils::formatFromUri)
        .collect(Collectors.toList());
  }

  protected List<MicroserviceInstance> createInstances(List<String> endpoints) {
    return endpoints.stream()
        .map(endpoint -> {
          MicroserviceInstance instance = new MicroserviceInstance();
          instance.setEndpoints(Collections.singletonList(endpoint));
          return instance;
        })
        .collect(Collectors.toList());
  }

  protected MicroserviceVersions createMicroserviceVersions(List<MicroserviceInstance> instances) {
    return new StaticMicroserviceVersions(
        DiscoveryManager.INSTANCE.getAppManager(),
        appId,
        microserviceName)
        .init(schemaByIdMap, VERSION, instances);
  }
}
