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

package org.apache.servicecomb.samples;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.zookeeper.ZookeeperClient;
import org.apache.servicecomb.config.zookeeper.ZookeeperConfig;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestSchema(schemaId = "ProviderController")
@RequestMapping(path = "/")
public class ProviderController implements InitializingBean {
  private Environment environment;

  private ZookeeperConfig zookeeperConfig;

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
    this.zookeeperConfig = new ZookeeperConfig(environment);
  }

  // a very simple service to echo the request parameter
  @GetMapping("/sayHello")
  public String sayHello(@RequestParam("name") String name) {
    return "Hello " + name;
  }

  @GetMapping("/getConfig")
  public String getConfig(@RequestParam("key") String key) {
    return environment.getProperty(key);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperConfig.getConnectString(),
        zookeeperConfig.getSessionTimeoutMillis(), zookeeperConfig.getConnectionTimeoutMillis(),
        new ExponentialBackoffRetry(1000, 3));
    client.start();
    client.blockUntilConnected(10, TimeUnit.SECONDS);

    String env = BootStrapProperties.readServiceEnvironment(environment);
    if (StringUtils.isEmpty(env)) {
      env = ZookeeperConfig.ZOOKEEPER_DEFAULT_ENVIRONMENT;
    }

    String path = String.format(ZookeeperClient.PATH_ENVIRONMENT, env);
    if (client.checkExists().forPath(path + "/config.properties") != null) {
      client.delete().forPath(path + "/config.properties");
    }
    client.create().creatingParentsIfNeeded().
        forPath(path + "/config.properties", "key1=1\nkey2=2".getBytes(StandardCharsets.UTF_8));

    path = String.format(ZookeeperClient.PATH_VERSION, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment),
        BootStrapProperties.readServiceVersion(environment));
    if (client.checkExists().forPath(path + "/config.properties") != null) {
      client.delete().forPath(path + "/config.properties");
    }
    client.create().creatingParentsIfNeeded().
        forPath(path + "/config.properties", "key2=3\nkey3=4".getBytes(StandardCharsets.UTF_8));

    path = String.format(ZookeeperClient.PATH_TAG, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment),
        BootStrapProperties.readServiceVersion(environment),
        zookeeperConfig.getInstanceTag());
    if (client.checkExists().forPath(path + "/config.properties") != null) {
      client.delete().forPath(path + "/config.properties");
    }
    client.create().creatingParentsIfNeeded().
        forPath(path + "/config.properties", "key2=3\nkey3=5".getBytes(StandardCharsets.UTF_8));

    client.close();
  }
}
