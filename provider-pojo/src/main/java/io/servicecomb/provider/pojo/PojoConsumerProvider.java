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

import io.servicecomb.core.provider.consumer.AbstractConsumerProvider;
import io.servicecomb.foundation.common.base.DescriptiveRunnable;
import io.servicecomb.foundation.common.base.RetryableRunnable;
import io.servicecomb.provider.pojo.reference.PojoConsumers;
import io.servicecomb.provider.pojo.reference.PojoReferenceMeta;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

@Component
public class PojoConsumerProvider extends AbstractConsumerProvider {
  private static final int DEFAULT_SLEEP_IN_MS = 2000;

  private final PojoConsumers pojoConsumers;
  private final ExecutorService executorService;
  private final int sleepInMs;

  @Inject
  PojoConsumerProvider(PojoConsumers pojoConsumers) {
    this(pojoConsumers, DEFAULT_SLEEP_IN_MS);
  }

  PojoConsumerProvider(PojoConsumers pojoConsumers, int sleepInMs) {
    this.pojoConsumers = pojoConsumers;
    this.executorService = Executors.newSingleThreadScheduledExecutor();
    this.sleepInMs = sleepInMs;
  }

  @Override
  public String getName() {
    return POJO;
  }

  @Override
  public void init() throws Exception {
    DescriptiveRunnable runnable = new InvocationCreationRunnable(pojoConsumers.getConsumerList());

    executorService.execute(new RetryableRunnable(runnable, sleepInMs));
  }

  private static class InvocationCreationRunnable implements DescriptiveRunnable {

    private final List<PojoReferenceMeta> consumers;

    private InvocationCreationRunnable(List<PojoReferenceMeta> consumers) {
      this.consumers = new LinkedList<>(consumers);
    }

    @Override
    public String description() {
      return "Pojo consumers invocation creation runnable";
    }

    @Override
    public void run() {
      for (
          Iterator<PojoReferenceMeta> iterator = consumers.iterator();
          iterator.hasNext();
          iterator.remove()) {

        iterator.next().createInvoker();
      }
    }
  }
}
