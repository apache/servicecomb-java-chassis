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

package org.apache.servicecomb.it.testcase;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.junit.Assert;
import org.junit.Test;

public class TestIgnoreStaticMethod {
  private MicroserviceMeta microserviceMeta = SCBEngine.getInstance().getProducerMicroserviceMeta();

  @Test
  public void ignoreStaticMethod_pojo() {
    SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta("ignoreStaticMethodPojoSchema");
    OperationMeta add = schemaMeta.findOperation("add");
    Assert.assertNotNull(add);

    OperationMeta sub = schemaMeta.findOperation("staticSub");
    Assert.assertNull(sub);
  }


  @Test
  public void ignoreStaticMethod_Jaxrs() {
    SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta("ignoreStaticMethodJaxrsSchema");
    OperationMeta add = schemaMeta.findOperation("add");
    Assert.assertNotNull(add);

    OperationMeta sub = schemaMeta.findOperation("staticSub");
    Assert.assertNull(sub);
  }

  @Test
  public void ignoreStaticMethod_Springmvc() {
    SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta("ignoreStaticMethodSpringmvcSchema");
    OperationMeta add = schemaMeta.findOperation("add");
    Assert.assertNotNull(add);

    OperationMeta sub = schemaMeta.findOperation("staticSub");
    Assert.assertNull(sub);
  }
}
