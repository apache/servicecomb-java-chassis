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

package org.apache.servicecomb.provider.springmvc.reference;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TestRequestMeta {
  ReferenceConfig referenceConfig = Mockito.mock(ReferenceConfig.class);

  RestOperationMeta swaggerRestOperation = Mockito.mock(RestOperationMeta.class);

  Map<String, String> pathParams = new HashMap<>();

  RequestMeta requestmeta = new RequestMeta(referenceConfig, swaggerRestOperation, pathParams);

  OperationMeta operationMeta = Mockito.mock(OperationMeta.class);

  @Test
  public void testGetReferenceConfig() {
    ReferenceConfig value = requestmeta.getReferenceConfig();
    Assert.assertNotNull(value);
  }

  @Test
  public void testGetPathParams() {
    Map<String, String> value = requestmeta.getPathParams();
    Assert.assertNotNull(value);
  }

  @Test
  public void testGetSwaggerRestOperation() {
    RestOperationMeta value = requestmeta.getSwaggerRestOperation();
    Assert.assertNotNull(value);
  }

  @Test
  public void testGetOperationMeta() {
    Assert.assertNull(requestmeta.getOperationMeta());
  }

  @Test
  public void testGetOperationQualifiedName() {
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("value");
    String qualifiedName = operationMeta.getSchemaQualifiedName();
    Assert.assertEquals("value", qualifiedName);
  }
}
