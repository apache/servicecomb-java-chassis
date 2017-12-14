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

package io.servicecomb.metrics.core.schedule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;

import io.servicecomb.metrics.core.model.InstanceModel;
import io.servicecomb.metrics.core.model.InvocationModel;
import io.servicecomb.metrics.core.model.RegistryModel;
import io.servicecomb.metrics.core.registry.InvocationThreadLocalCache;
import io.servicecomb.metrics.core.registry.ThreadLocalMonitorManager;

public class DefaultStatisticsRunner extends AbstractStatisticsRunner {

  private final AtomicReference<RegistryModel> registryModel = new AtomicReference<>(new RegistryModel());

  private final Map<String, InvocationThreadLocalCache> lastRunInvocationModels;

  private final AtomicLong lastRunNanoTime;

  @Override
  public RegistryModel getRegistryModel() {
    return registryModel.get();
  }

  @Autowired
  public DefaultStatisticsRunner() {
    super();
    this.lastRunNanoTime = new AtomicLong(System.nanoTime());
    this.lastRunInvocationModels = new HashMap<>();
  }

  @Override
  public void run() {
    List<InvocationThreadLocalCache> allLocalModels = ThreadLocalMonitorManager.getAllInvocationThreadLocalCache();
    long currentTime = System.nanoTime();
    Map<String, InvocationThreadLocalCache> mergedLocalModels = new HashMap<>();
    for (InvocationThreadLocalCache localModel : allLocalModels) {
      InvocationThreadLocalCache operationLocalModel = mergedLocalModels.computeIfAbsent(localModel.getOperationName(),
          m -> new InvocationThreadLocalCache(localModel.getOperationName()));
      //merge all local model into one per operation
      operationLocalModel.merge(localModel);
    }

    //for instance level
    double lifeTimeInQueueMax = 0;
    double lifeTimeInQueueMin = 0;
    double totalLifeTimeInQueue = 0;
    double executionTimeMax = 0;
    double executionTimeMin = 0;
    double totalExecutionTimeAverage = 0;
    double totalProviderCallCount = 0;
    long totalCountInQueue = 0;

    // unused before compute tps and latency
    //double escapedSecondTime = (double)(currentTime - lastRunNanoTime.get()) / 1000000000;

    Map<String, InvocationModel> newInvocationModels = new ConcurrentHashMap<>();
    for (InvocationThreadLocalCache mergedLocalModel : mergedLocalModels.values()) {
      InvocationThreadLocalCache lastModel = lastRunInvocationModels
          .getOrDefault(mergedLocalModel.getOperationName(),
              new InvocationThreadLocalCache(mergedLocalModel.getOperationName()));
      long providerCallCount = mergedLocalModel.getProviderCallCount() - lastModel.getProviderCallCount();
      double averageExecuteTime =
          (mergedLocalModel.getExecutionTime() - lastModel.getExecutionTime()) / providerCallCount;
      double averageLifeTimeInQueue =
          (mergedLocalModel.getLifeTimeInQueue() - lastModel.getLifeTimeInQueue()) / providerCallCount;

      newInvocationModels.put(mergedLocalModel.getOperationName(),
          new InvocationModel(mergedLocalModel.getOperationName(), mergedLocalModel.getCountInQueue(),
              mergedLocalModel.getLifeTimeInQueueMax(), mergedLocalModel.getLifeTimeInQueueMin(),
              averageLifeTimeInQueue,
              mergedLocalModel.getExecutionTimeMax(), mergedLocalModel.getExecutionTimeMin(), averageExecuteTime));

      if (isValidMax(lifeTimeInQueueMax, mergedLocalModel.getLifeTimeInQueueMax())) {
        lifeTimeInQueueMax = mergedLocalModel.getLifeTimeInQueueMax();
      }
      if (isValidMin(lifeTimeInQueueMin, mergedLocalModel.getLifeTimeInQueueMin())) {
        lifeTimeInQueueMin = mergedLocalModel.getLifeTimeInQueueMin();
      }
      if (isValidMax(executionTimeMax, mergedLocalModel.getExecutionTimeMax())) {
        executionTimeMax = mergedLocalModel.getExecutionTimeMax();
      }
      if (isValidMin(executionTimeMin, mergedLocalModel.getExecutionTimeMin())) {
        executionTimeMin = mergedLocalModel.getExecutionTimeMin();
      }

      totalLifeTimeInQueue += mergedLocalModel.getLifeTimeInQueue() - lastModel.getLifeTimeInQueue();
      totalExecutionTimeAverage += mergedLocalModel.getExecutionTime() - lastModel.getExecutionTime();
      totalProviderCallCount += mergedLocalModel.getProviderCallCount();
      totalCountInQueue += mergedLocalModel.getCountInQueue();

      //update last compute cache
      lastRunInvocationModels.put(mergedLocalModel.getOperationName(), mergedLocalModel);
    }

    InstanceModel newInstanceModel = new InstanceModel(totalCountInQueue, lifeTimeInQueueMax, lifeTimeInQueueMin,
        totalLifeTimeInQueue / totalProviderCallCount,
        executionTimeMax, executionTimeMin,
        totalExecutionTimeAverage / totalProviderCallCount);

    RegistryModel newRegistryModel = new RegistryModel(newInstanceModel, newInvocationModels);

    //update registry model once
    registryModel.set(newRegistryModel);

    //update last compute time
    lastRunNanoTime.set(currentTime);
  }

  private boolean isValidMax(double source, double value) {
    return source < value && value != 0;
  }

  private boolean isValidMin(double source, double value) {
    return source == 0 || (source > value && value != 0);
  }
}
