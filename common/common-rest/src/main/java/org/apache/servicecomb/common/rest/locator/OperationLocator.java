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
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 从path和http method定位到具体的operation
 */
public class OperationLocator {
  private static final Logger LOGGER = LoggerFactory.getLogger(OperationLocator.class);

  private static final String SLASH = "/";

  protected RestOperationMeta operation;

  protected Map<String, String> pathVarMap = new HashMap<>();

  protected boolean resourceFound = false;

  public RestOperationMeta getOperation() {
    return this.operation;
  }

  public Map<String, String> getPathVarMap() {
    return this.pathVarMap;
  }

  // 先在静态路径operation list中查找；如果找不到，则在动态路径operation list中查找
  public void locate(String microserviceName, String path, String httpMethod, MicroservicePaths microservicePaths) {
    // 在静态路径中查找
    operation = locateStaticPathOperation(path, httpMethod, microservicePaths.getStaticPathOperationMap());
    if (operation != null) {
      // 全部定位完成
      return;
    }

    // 在动态路径中查找
    operation = locateDynamicPathOperation(path, microservicePaths.getDynamicPathOperationList(), httpMethod);
    if (operation != null) {
      return;
    }

    Status status = Status.NOT_FOUND;
    if (resourceFound) {
      status = Status.METHOD_NOT_ALLOWED;
    }
    LOGGER.error("locate path failed, status:{}, http method:{}, path:{}, microserviceName:{}",
        status,
        httpMethod,
        path,
        microserviceName);
    throw new InvocationException(status, status.getReasonPhrase());
  }

  protected RestOperationMeta locateStaticPathOperation(String path, String httpMethod,
      Map<String, OperationGroup> staticPathOperations) {
    OperationGroup group = staticPathOperations.get(path);
    if (group == null) {
      return null;
    }

    resourceFound = true;
    return group.findValue(httpMethod);
  }

  protected RestOperationMeta locateDynamicPathOperation(String path, Collection<RestOperationMeta> resourceList,
      String httpMethod) {
    for (RestOperationMeta resource : resourceList) {
      String remainPath = resource.getAbsolutePathRegExp().match(path, pathVarMap);
      // 刚好匹配，不多也不少
      if ("".equals(remainPath)) {
        resourceFound = true;
        if (checkHttpMethod(resource, httpMethod)) {
          return resource;
        }
      }
    }
    return null;
  }

  protected boolean checkHttpMethod(RestOperationMeta operation, String httpMethod) {
    return operation.getHttpMethod().equals(httpMethod);
  }

  // TODO: almost always change path, this make performance lower.
  // Path: /a/b/c -> /a/b/c/
  static String getStandardPath(String path) {
    if (path.length() > 0 && !path.endsWith(SLASH)) {
      path += SLASH;
    }
    return path;
  }
}
