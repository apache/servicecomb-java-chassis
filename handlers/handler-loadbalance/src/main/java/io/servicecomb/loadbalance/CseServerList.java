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

package io.servicecomb.loadbalance;

import java.util.List;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

/**
 * 通过RegistryUtils查询服务器列表。 RegistryUtils本身具备缓存和刷新不可用服务器的功能，因此这里不需要进行列表缓存和状态检测。
 */
public class CseServerList implements ServerList<Server> {
  private ServerListCache serverListCache;

  public CseServerList(String appId, String microserviceName, String microserviceVersionRule,
      String transportName) {
    serverListCache = new ServerListCache(appId, microserviceName, microserviceVersionRule, transportName);
  }

  @Override
  public List<Server> getInitialListOfServers() {
    return serverListCache.getLatestEndpoints();
  }

  @Override
  public List<Server> getUpdatedListOfServers() {
    return getInitialListOfServers();
  }
}
