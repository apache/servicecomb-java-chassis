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

package org.apache.servicecomb.core.definition.loader;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.common.config.PaaSResourceUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/*
 * 场景：
 * 1.consumer
 *   网管调用产品
 *   网管事先不知道产品对应的微服务名
 *   产品注册到网管后，网管根据注册信息，进行契约注册
 * 2.producer
 *   需要支持在不同的产品中部署为不同的微服务名
 *   微服务名是由环境变量等等方式注入的
 *   此时可以在BootListener中进行注册（必须在producer初始化之前注册契约）
 *
 * @Deprecated This class is deprecated because when making calls to a provider, schemas will be downloaded from service enter.
 * And at provider, schemas will register to service center when starting up.
 */
@Deprecated
public class DynamicSchemaLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicSchemaLoader.class);

  public static final DynamicSchemaLoader INSTANCE = new DynamicSchemaLoader();

  private DynamicSchemaLoader() {
  }

  /**
   * 动态注册指定目录下的schema契约到当前服务
   * @param schemaLocation eg. "classpath*:schemas/*.yaml"
   */
  public void registerSchemas(String schemaLocation) {
    registerSchemas(RegistryUtils.getMicroservice().getServiceName(), schemaLocation);
  }

  /**
   * 动态注册指定目录下的schema契约到指定服务
   * @param microserviceName name of microservice
   * @param schemaLocation eg. "classpath*:schemas/*.yaml"
   */
  public void registerSchemas(String microserviceName, String schemaLocation) {
    LOGGER.info("dynamic register schemas for {} in {}", microserviceName, schemaLocation);

    List<SchemaMeta> schemaMetaList = new ArrayList<>();
    Resource[] resArr = PaaSResourceUtils.getResources(schemaLocation);
    for (Resource resource : resArr) {
      SchemaMeta schemaMeta =
          CseContext.getInstance().getSchemaLoader().registerSchema(microserviceName, resource);

      schemaMetaList.add(schemaMeta);
    }

    CseContext.getInstance().getSchemaListenerManager().notifySchemaListener(schemaMetaList);
  }
}
