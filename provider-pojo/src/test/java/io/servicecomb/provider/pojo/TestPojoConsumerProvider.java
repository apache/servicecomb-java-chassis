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

package io.servicecomb.provider.pojo;

import static io.servicecomb.provider.pojo.PojoConst.POJO;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.servicecomb.provider.pojo.reference.PojoConsumers;
import io.servicecomb.provider.pojo.reference.PojoReferenceMeta;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class TestPojoConsumerProvider {

  private final PojoReferenceMeta meta1 = mock(PojoReferenceMeta.class);
  private final PojoReferenceMeta meta2 = mock(PojoReferenceMeta.class);

  private final PojoConsumers pojoConsumers = new PojoConsumers();


  private final RuntimeException exception = new RuntimeException("oops");
  private final PojoConsumerProvider pojoConsumerProvider = new PojoConsumerProvider(pojoConsumers, 20);

  @Before
  public void setUp() throws Exception {
    pojoConsumers.addPojoReferenceMeta(meta1);
    pojoConsumers.addPojoReferenceMeta(meta2);
  }

  @Test
  public void providerNameIsPojo() throws Exception {
    assertThat(pojoConsumerProvider.getName(), is(POJO));
  }

  @Test
  public void pojoConsumersAreCalledOnlyOnce() throws Exception {
    pojoConsumerProvider.init();

    TimeUnit.MILLISECONDS.sleep(200);

    verify(meta1).createInvoker();
    verify(meta2).createInvoker();
  }

  @Test
  public void createsInvokerUntilSuccess() throws Exception {
    doThrow(exception).doNothing().when(meta1).createInvoker();

    pojoConsumerProvider.init();

    TimeUnit.MILLISECONDS.sleep(200);

    verify(meta1, times(2)).createInvoker();
    verify(meta2).createInvoker();
  }

  @Test
  public void eachInvokerIsCreatedOnlyOnce() throws Exception {
    doThrow(exception).doNothing().when(meta2).createInvoker();

    pojoConsumerProvider.init();

    TimeUnit.MILLISECONDS.sleep(200);

    verify(meta1).createInvoker();
    verify(meta2, times(2)).createInvoker();
  }
}
