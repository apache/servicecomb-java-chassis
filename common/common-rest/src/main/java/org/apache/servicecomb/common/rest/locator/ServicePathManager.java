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

package org.apache.servicecomb.common.rest.locator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 对静态路径和动态路径的operation进行预先处理，加速operation的查询定位
 */
public class ServicePathManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServicePathManager.class);

  private static final String REST_PATH_MANAGER = "RestServicePathManager";

  protected MicroserviceMeta microserviceMeta;

  // equal to swagger
  protected MicroservicePaths swaggerPaths = new MicroservicePaths();

  // we support swagger basePath is not include contextPath and urlPattern
  // so for producer, we must concat contextPath and urlPattern
  // only valid for microservice of this process
  protected MicroservicePaths producerPaths;

  // 已经有哪些schemaId的path信息加进来了
  // 在producer场景中，业务before producer provider事件中将契约注册进来，此时会触发事件，携带注册范围的信息
  // 启动流程的最后阶段，同样会触发一次事件，此时是全量的信息
  // 所以，可能会重复
  protected Set<String> schemaIdSet = new HashSet<>();

  public static ServicePathManager getServicePathManager(MicroserviceMeta microserviceMeta) {
    return microserviceMeta.getExtData(REST_PATH_MANAGER);
  }

  public void saveToMicroserviceMeta() {
    microserviceMeta.putExtData(REST_PATH_MANAGER, this);
  }

  public ServicePathManager(MicroserviceMeta microserviceMeta) {
    this.microserviceMeta = microserviceMeta;
  }

  public MicroserviceMeta getMicroserviceMeta() {
    return microserviceMeta;
  }

  public boolean isSchemaExists(String schemaId) {
    return schemaIdSet.contains(schemaId);
  }

  public void addSchema(SchemaMeta schemaMeta) {
    if (isSchemaExists(schemaMeta.getSchemaId())) {
      return;
    }

    schemaIdSet.add(schemaMeta.getSchemaId());
    for (OperationMeta operationMeta : schemaMeta.getOperations()) {
      RestOperationMeta restOperationMeta = new RestOperationMeta();
      restOperationMeta.init(operationMeta);
      operationMeta.putExtData(RestConst.SWAGGER_REST_OPERATION, restOperationMeta);
      addResource(restOperationMeta);
    }

    LOGGER.info("add schema to service paths. {}:{}:{}.",
        schemaMeta.getMicroserviceMeta().getAppId(),
        schemaMeta.getMicroserviceName(),
        schemaMeta.getSchemaId());
  }

  public ServicePathManager cloneServicePathManager() {
    ServicePathManager mgr = new ServicePathManager(microserviceMeta);
    swaggerPaths.cloneTo(mgr.swaggerPaths);
    mgr.schemaIdSet.addAll(schemaIdSet);
    return mgr;
  }

  public OperationLocator consumerLocateOperation(String path, String httpMethod) {
    String standPath = OperationLocator.getStandardPath(path);
    OperationLocator locator = new OperationLocator();
    locator.locate(microserviceMeta.getName(), standPath, httpMethod, swaggerPaths);

    return locator;
  }

  public OperationLocator producerLocateOperation(String path, String httpMethod) {
    String standPath = OperationLocator.getStandardPath(path);
    OperationLocator locator = new OperationLocator();
    locator.locate(microserviceMeta.getName(), standPath, httpMethod, producerPaths);

    return locator;
  }

  public void addResource(RestOperationMeta swaggerRestOperation) {
    swaggerPaths.addResource(swaggerRestOperation);
  }

  public void sortPath() {
    swaggerPaths.sortPath();
  }

  public void buildProducerPaths() {
    String urlPrefix = System.getProperty(Const.URL_PREFIX);
    if (StringUtils.isEmpty(urlPrefix)) {
      producerPaths = swaggerPaths;
      producerPaths.printPaths();
      return;
    }

    producerPaths = new MicroservicePaths();
    for (OperationGroup operationGroup : swaggerPaths.getStaticPathOperationMap().values()) {
      addProducerPaths(urlPrefix, operationGroup.values());
    }

    addProducerPaths(urlPrefix, swaggerPaths.getDynamicPathOperationList());
    producerPaths.printPaths();
  }

  private void addProducerPaths(String urlPrefix, Collection<RestOperationMeta> restOperationMetas) {
    for (RestOperationMeta swaggerRestOperation : restOperationMetas) {
      RestOperationMeta producerRestOperation = swaggerRestOperation;
      if (!swaggerRestOperation.getAbsolutePath().startsWith(urlPrefix)) {
        producerRestOperation = new RestOperationMeta();
        producerRestOperation.init(swaggerRestOperation.getOperationMeta());
        producerRestOperation.setAbsolutePath(urlPrefix + swaggerRestOperation.getAbsolutePath());
      }
      producerPaths.addResource(producerRestOperation);
    }
  }
}
