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
package org.apache.servicecomb.core.executor;

import java.util.concurrent.Executor;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestExecutorManager {
  @Mocked
  Executor defaultExecutor;

  @Before
  public void setup() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void findExecutor_oneParam(@Mocked Executor executor, @Mocked OperationMeta operationMeta) {
    new Expectations(BeanUtils.class) {
      {
        BeanUtils.getBean(ExecutorManager.EXECUTOR_DEFAULT);
        result = executor;
      }
    };

    Assertions.assertSame(executor, new ExecutorManager().findExecutor(operationMeta));
  }

  @Test
  public void findExecutor_twoParam_opCfg_withoutOpDef(@Mocked Executor executor,
      @Mocked OperationMeta operationMeta) {
    // String schemaQualifiedName = "schemaId.opId";
    String microserviceQualifiedName = "microserviceName.schemaId.opId";
    String opBeanId = "opBeanId";
    ArchaiusUtils.setProperty(ExecutorManager.KEY_EXECUTORS_PREFIX + microserviceQualifiedName, opBeanId);
    new Expectations(BeanUtils.class) {
      {
        operationMeta.getMicroserviceQualifiedName();
        result = microserviceQualifiedName;
        BeanUtils.getBean(opBeanId);
        result = executor;
      }
    };

    Assertions.assertSame(executor, new ExecutorManager().findExecutor(operationMeta, null));
  }

  @Test
  public void findExecutor_twoParam_opCfg_withOpDef(@Mocked Executor executor,
      @Mocked Executor defExecutor,
      @Mocked OperationMeta operationMeta) {
    String microserviceQualifiedName = "microserviceName.schemaId.opId";
    String opBeanId = "opBeanId";
    ArchaiusUtils.setProperty(ExecutorManager.KEY_EXECUTORS_PREFIX + microserviceQualifiedName, opBeanId);
    new Expectations(BeanUtils.class) {
      {
        operationMeta.getMicroserviceQualifiedName();
        result = microserviceQualifiedName;
        BeanUtils.getBean(opBeanId);
        result = executor;
      }
    };

    Assertions.assertSame(executor, new ExecutorManager().findExecutor(operationMeta, defExecutor));
  }

  @Test
  public void findExecutor_twoParam_schemaCfg_withOpDef(@Mocked OperationMeta operationMeta,
      @Mocked Executor defExecutor) {
    Assertions.assertSame(defExecutor, new ExecutorManager().findExecutor(operationMeta, defExecutor));
  }

  @Test
  public void findExecutor_twoParam_schemaCfg_withoutOpDef(@Mocked Executor executor,
      @Mocked OperationMeta operationMeta) {
    String microserviceName = "serviceName";
    String schemaName = "schemaId";
    String opBeanId = "opBeanId";
    ArchaiusUtils.setProperty(ExecutorManager.KEY_EXECUTORS_PREFIX + microserviceName + "." +  schemaName, opBeanId);
    new Expectations(BeanUtils.class) {
      {
        operationMeta.getSchemaId();
        result = schemaName;
        operationMeta.getMicroserviceName();
        result = microserviceName;
        BeanUtils.getBean(opBeanId);
        result = executor;
      }
    };
    Assertions.assertSame(executor, new ExecutorManager().findExecutor(operationMeta, null));
  }

  @Test
  public void findExecutor_twoParam_defaultCfg(@Mocked Executor executor,
      @Mocked SchemaMeta schemaMeta,
      @Mocked OperationMeta operationMeta) {
    String beanId = "beanId";
    ArchaiusUtils.setProperty(ExecutorManager.KEY_EXECUTORS_DEFAULT, beanId);
    new Expectations(BeanUtils.class) {
      {
        BeanUtils.getBean(beanId);
        result = executor;
      }
    };

    Assertions.assertSame(executor, new ExecutorManager().findExecutor(operationMeta, null));
  }
}
