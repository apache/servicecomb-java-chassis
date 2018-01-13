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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.common.rest.definition.RestOperationComparator;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicroservicePaths {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroservicePaths.class);

  // 运行阶段,静态path,一次直接查找到目标,不必遍历查找
  // 以path为key
  protected Map<String, OperationGroup> staticPathOperations = new HashMap<>();

  // 运行阶段,以path优先级,从高到低排列的operation列表
  protected List<RestOperationMeta> dynamicPathOperationsList = new ArrayList<>();

  public void cloneTo(MicroservicePaths other) {
    other.staticPathOperations.putAll(staticPathOperations);
    other.dynamicPathOperationsList.addAll(dynamicPathOperationsList);
  }

  public void sortPath() {
    RestOperationComparator comparator = new RestOperationComparator();
    Collections.sort(this.dynamicPathOperationsList, comparator);
  }

  public void addResource(RestOperationMeta swaggerRestOperation) {
    if (swaggerRestOperation.isAbsoluteStaticPath()) {
      // 静态path
      addStaticPathResource(swaggerRestOperation);
      return;
    }

    dynamicPathOperationsList.add(swaggerRestOperation);
  }

  protected void addStaticPathResource(RestOperationMeta operation) {
    String httpMethod = operation.getHttpMethod();
    String path = operation.getAbsolutePath();
    OperationGroup group = staticPathOperations.get(path);
    if (group == null) {
      group = new OperationGroup();
      group.register(httpMethod, operation);
      staticPathOperations.put(path, group);
      return;
    }

    if (group.findValue(httpMethod) == null) {
      group.register(httpMethod, operation);
      return;
    }

    throw new ServiceCombException(
        String.format("operation with url %s, method %s is duplicated.", path, httpMethod));
  }

  public Map<String, OperationGroup> getStaticPathOperationMap() {
    return staticPathOperations;
  }

  public List<RestOperationMeta> getDynamicPathOperationList() {
    return dynamicPathOperationsList;
  }

  public void printPaths() {
    for (Entry<String, OperationGroup> entry : staticPathOperations.entrySet()) {
      OperationGroup operationGroup = entry.getValue();
      printPath(operationGroup.values());
    }

    printPath(getDynamicPathOperationList());
  }

  protected void printPath(Collection<RestOperationMeta> operations) {
    for (RestOperationMeta operation : operations) {
      LOGGER.info("Swagger mapped \"{[{}], method=[{}], produces={}}\" onto {}",
          operation.getAbsolutePath(),
          operation.getHttpMethod(),
          operation.getProduces(),
          operation.getOperationMeta().getMethod());
    }
  }
}
