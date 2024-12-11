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

package org.apache.servicecomb.swagger.invocation.ws;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialExecutorWrapper implements Executor {

  private static final Logger LOGGER = LoggerFactory.getLogger(SerialExecutorWrapper.class);

  private static final int LEAST_QUEUE_CAPACITY = 100;

  private final InvocationType invocationType;

  private final String id;

  private final Queue<Runnable> queue;

  private final Executor workerPool;

  private final int queueCapacity;

  private final int drainThreshold;

  private final int fullThreshold;

  private final AtomicBoolean scheduleFlag;

  private final int maxContinueTimes;

  private QueueDrainSubscriber queueDrainSubscriber;

  private QueueFullSubscriber queueFullSubscriber;

  public SerialExecutorWrapper(
      InvocationType invocationType,
      String id, Executor workerPool, int queueCapacity, int maxContinueTimes) {
    this.invocationType = invocationType;
    this.id = id;
    this.workerPool = workerPool;
    this.queueCapacity = correctQueueCapacity(queueCapacity);
    queue = new ArrayBlockingQueue<>(calculateRealQueueSize(), true);
    drainThreshold = calculateDrainThreshold();
    fullThreshold = calculateFullThreshold();
    scheduleFlag = new AtomicBoolean();
    this.maxContinueTimes = correctMaxContinueTimes(maxContinueTimes);
  }

  /**
   * Subscribe queue drain event to resume message queue.
   * Use this method in cooperation with {@link #subscribeQueueFullEvent(QueueFullSubscriber)} method.
   */
  public void subscribeQueueDrainEvent(QueueDrainSubscriber subscriber) {
    if (subscriber != null) {
      queueDrainSubscriber = subscriber;
    }
  }

  /**
   * Subscribe queue full event to pause message queue.
   * Use this method in cooperation with {@link #subscribeQueueDrainEvent(QueueDrainSubscriber)} method.
   */
  public void subscribeQueueFullEvent(QueueFullSubscriber subscriber) {
    if (subscriber != null) {
      queueFullSubscriber = subscriber;
    }
  }

  @Override
  public void execute(Runnable command) {
    Objects.requireNonNull(command, "command must not be null");
    Objects.requireNonNull(queueDrainSubscriber, "queueDrainSubscriber must not be null");
    queue.add(command);
    markQueueFull();
    scheduleWorker();
  }

  private void scheduleWorker() {
    if (!scheduleFlag.compareAndSet(false, true)) {
      // a running task has been set, don't repeat
      return;
    }
    try {
      workerPool.execute(() -> {
        try {
          runTasks();
        } finally {
          scheduleFlag.set(false);
          if (!queue.isEmpty()) {
            scheduleWorker();
          }
        }
      });
    } catch (Throwable e) {
      // in case that the underlying executor queue full, the scheduleFlag should be recovered
      scheduleFlag.set(false);
      LOGGER.error("[{}]-[{}] failed to execute task in actual thread pool!", invocationType, id, e);
    }
  }

  private void runTasks() {
    int workCount = 1;
    while (true) {
      final Runnable task = queue.poll();
      if (task == null) {
        break;
      }

      try {
        task.run();
      } catch (Throwable e) {
        LOGGER.error("[{}]-[{}] error occurred while executing task[{}]", invocationType, id, task, e);
      }

      ++workCount;
      if (workCount > maxContinueTimes) {
        break;
      }
    }

    notifyIfQueueDrain();
  }

  private void markQueueFull() {
    final QueueFullSubscriber subscriber = queueFullSubscriber;
    if (subscriber == null) {
      return;
    }

    if ((queue.size() < fullThreshold)) {
      return;
    }

    LOGGER.warn("[{}]-[{}] queue nearly full! queue length: {}/{}",
        invocationType, id, queue.size(), calculateRealQueueSize());
    try {
      subscriber.run();
    } catch (Throwable e) {
      LOGGER.error("[{}]-[{}] error occurred while notifying queue full subscriber[{}]", invocationType, id,
          subscriber, e);
    }
    if (queue.size() < drainThreshold) {
      // in case that all tasks has been completed and WebSocket is paused,
      // and no task to trigger the WebSocket get resumed
      execute(this::notifyIfQueueDrain);
    }
  }

  private void notifyIfQueueDrain() {
    final QueueDrainSubscriber subscriber = queueDrainSubscriber;
    if (subscriber == null) {
      return;
    }
    if (queue.size() > drainThreshold) {
      return;
    }
    try {
      subscriber.run();
    } catch (Throwable e) {
      LOGGER.error("[{}]-[{}] error occurred while notifying queue drain subscriber[{}]", invocationType, id,
          subscriber, e);
    }
  }

  private int calculateRealQueueSize() {
    return (int) (queueCapacity * 1.2);
  }

  private int calculateDrainThreshold() {
    return (int) (queueCapacity * 0.25);
  }

  private int calculateFullThreshold() {
    return queueCapacity;
  }

  private int correctQueueCapacity(int queueCapacity) {
    if (queueCapacity < LEAST_QUEUE_CAPACITY) {
      LOGGER.warn("queue capacity less than 10 does not make sense, adjust to {}", LEAST_QUEUE_CAPACITY);
      return LEAST_QUEUE_CAPACITY;
    }
    return queueCapacity;
  }

  private int correctMaxContinueTimes(int maxContinueTimes) {
    if (maxContinueTimes < 1) {
      LOGGER.warn("maxContinueTimes less than 1 does not make sense, adjust to 1");
      return 1;
    }
    return maxContinueTimes;
  }

  public interface QueueDrainSubscriber {
    void run();
  }

  public interface QueueFullSubscriber {
    void run();
  }
}
