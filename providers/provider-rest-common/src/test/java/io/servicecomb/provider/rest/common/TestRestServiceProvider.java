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

package io.servicecomb.provider.rest.common;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.core.definition.schema.ProducerSchemaFactory;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.ReflectUtils;
import mockit.Mock;
import mockit.MockUp;

public class TestRestServiceProvider {

  @Test
  public void testInit() throws Exception {
    ApplicationContext context = Mockito.mock(ApplicationContext.class);
    Mockito.when(context.getBeansWithAnnotation(RestSchema.class)).thenReturn(new HashMap<String, Object>());

    new MockUp<BeanUtils>() {
      @Mock
      ApplicationContext getContext() {
        return context;
      }
    };

    RestProducerProvider restProducerProvider = new RestProducerProvider();
    ReflectUtils.setField(restProducerProvider, "producerSchemaFactory", new ProducerSchemaFactory());
    ReflectUtils.setField(restProducerProvider, "restProducers", new RestProducers());

    restProducerProvider.init();
    Assert.assertEquals(RestConst.REST, restProducerProvider.getName());
  }

  @Test
  public void testInvoke() throws Exception {
    //        Invocation invocation = Mockito.mock(Invocation.class);
    //        AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
    //        OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    //        Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    //        RestProviderOperation restProviderOperationMeta = Mockito.mock(RestProviderOperation.class);
    //        Mockito.when(operationMeta.getExtData("rest.operation")).thenReturn(restProviderOperationMeta);
    //        ArgsMapper argsMapper = Mockito.mock(ArgsMapper.class);
    //        Mockito.when(restProviderOperationMeta.getArgsMapper()).thenReturn(argsMapper);
    //        try {
    //            RestProducerProvider.getInstance().invoke(invocation, asyncResp);
    //        } catch (Exception e) {
    //            Assert.assertEquals(null, e.getMessage());
    //        }
    //        Assert.assertEquals(invocation.getContext(), ContextUtils.getInvocationContext().getContext());
  }
}
