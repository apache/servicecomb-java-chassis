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

package io.servicecomb.provider.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;

import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.provider.consumer.ConsumerProviderManager;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.core.provider.producer.AbstractProducerProvider;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.provider.pojo.PojoConsumerProvider;
import io.servicecomb.provider.pojo.TestPojoConsumerProvider;
import io.servicecomb.provider.pojo.instance.PojoInstanceFactory;
import io.servicecomb.provider.pojo.reference.PojoReferenceMeta;
import io.servicecomb.provider.pojo.schema.PojoProducerMeta;
import mockit.Mock;
import mockit.MockUp;

public class MockUtil {
  MicroserviceMetaManager microserviceMetaManager = new MicroserviceMetaManager();

  private static MockUtil instance = new MockUtil();

  private MockUtil() {

  }

  public static MockUtil getInstance() {
    return instance;
  }

  public void mockProxy() {

    new MockUp<Proxy>() {
      @Mock
      Object newProxyInstance(ClassLoader loader,
          Class<?>[] interfaces,
          InvocationHandler h) throws IllegalArgumentException {
        return h;
      }
    };
  }

  public void mockBeanUtils() {

    new MockUp<BeanUtils>() {
      @Mock
      ApplicationContext getContext() {
        return Mockito.mock(ApplicationContext.class);
      }

      @Mock
      <T> T getBean(String name) {
        return null;
      }
    };
  }

  public void mockBeanUtilsObject() {

    new MockUp<BeanUtils>() {
      @Mock
      ApplicationContext getContext() {
        return Mockito.mock(ApplicationContext.class);
      }

      @SuppressWarnings("unchecked")
      @Mock
      <T> T getBean(String name) {
        return (T) new Object();
      }
    };
  }

  public void mockRegisterManager() throws InstantiationException, IllegalAccessException {
    MicroserviceMeta microserviceMeta = new MicroserviceMeta("app:test");
    microserviceMetaManager.register("test", microserviceMeta);
  }

  public void mockMicroserviceMeta() {

    new MockUp<MicroserviceMeta>() {
      @Mock
      public SchemaMeta ensureFindSchemaMeta(String schemaId) {
        SchemaMeta lSchemaMeta = Mockito.mock(SchemaMeta.class);
        Mockito.when(lSchemaMeta.getSwaggerIntf())
            .thenAnswer(new Answer<Class<? extends TestPojoConsumerProvider>>() {
              @Override
              public Class<? extends TestPojoConsumerProvider> answer(
                  InvocationOnMock invocation) throws Throwable {
                return TestPojoConsumerProvider.class;
              }
            });
        return lSchemaMeta;
      }

      @Mock
      public SchemaMeta ensureFindSchemaMeta(Class<?> schemaIntf) {
        return Mockito.mock(SchemaMeta.class);
      }
    };
  }

  public void mockPojoReferenceMeta() {

    new MockUp<PojoReferenceMeta>() {
      @Mock
      public void init() {

      }

      @Mock
      public SchemaMeta getSchemaMeta() {
        return Mockito.mock(SchemaMeta.class);
      }
    };
  }

  public void mockConsumerProviderManager() {
    new MockUp<ConsumerProviderManager>() {
      @Mock
      public ReferenceConfig getReferenceConfig(String microserviceName) {
        return null;
      }
    };
  }

  public void mockSchemaMeta() {

    new MockUp<SchemaMeta>() {
      @Mock
      public Class<?> getIntf() {
        return Object.class;
      }
    };
  }

  public void mockPojoInstanceFactory() {

    new MockUp<PojoInstanceFactory>() {
      @Mock
      public Object create(String className) {
        return Object.class;
      }
    };
  }

  public void mockPojoConsumerProvider() {

    new MockUp<PojoConsumerProvider>() {
      @Mock
      private void createInvoker(PojoReferenceMeta pojoReference) {

      }
    };
  }

  public void mockAbstractServiceProvider() {

    new MockUp<AbstractProducerProvider>() {
      @SuppressWarnings("unchecked")
      @Mock
      protected <T> T findProviderSchema(OperationMeta operationMeta) {
        PojoProducerMeta lPojoSchemaMeta = Mockito.mock(PojoProducerMeta.class);
        Mockito.when(lPojoSchemaMeta.getInstance()).thenReturn(lPojoSchemaMeta);
        return (T) lPojoSchemaMeta;
      }
    };
  }
}
