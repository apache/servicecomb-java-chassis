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
package org.apache.servicecomb.foundation.common.event;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.apache.servicecomb.foundation.common.utils.LambdaMetafactoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;

public class SimpleSubscriber {
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSubscriber.class);

  private Object instance;

  private Method method;

  private int order;

  // generated from method
  private Consumer<Object> lambda;

  private Consumer<Object> dispatcher;

  public SimpleSubscriber(Object instance, Method method) {
    this.instance = instance;
    this.method = method;

    SubscriberOrder subscriberOrder = method.getAnnotation(SubscriberOrder.class);
    if (subscriberOrder != null) {
      order = subscriberOrder.value();
    }

    method.setAccessible(true);
    try {
      lambda = LambdaMetafactoryUtils.createLambda(instance, method, Consumer.class);
    } catch (Throwable throwable) {
      // because enhance LambdaMetafactoryUtils to support ALL_MODES by reflect
      // never run into this branch.
      // otherwise create a listener instance of anonymous class will run into this branch
      LOGGER.warn("Failed to create lambda for method: {}, fallback to reflect.", method);
      lambda = event -> {
        try {
          method.invoke(instance, event);
        } catch (Throwable e) {
          throw new IllegalStateException(e);
        }
      };
    }

    dispatcher = this::syncDispatch;
    if (method.getAnnotation(AllowConcurrentEvents.class) != null) {
      dispatcher = this::concurrentDispatch;
    }
  }

  public Object getInstance() {
    return instance;
  }

  public Method getMethod() {
    return method;
  }

  public int getOrder() {
    return order;
  }

  public void dispatchEvent(Object event) {
    try {
      dispatcher.accept(event);
    } catch (Throwable e) {
      LOGGER.error("event process should not throw error. ", e);
    }
  }

  private void syncDispatch(Object event) {
    synchronized (this) {
      lambda.accept(event);
    }
  }

  private void concurrentDispatch(Object event) {
    lambda.accept(event);
  }
}
