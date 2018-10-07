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
package org.apache.servicecomb.metrics.core.publish.model;

import java.util.HashMap;
import java.util.Map;

public class DefaultPublishModel {
  private ConsumerPublishModel consumer = new ConsumerPublishModel();

  private ProducerPublishModel producer = new ProducerPublishModel();

  private EdgePublishModel edge = new EdgePublishModel();

  private Map<String, ThreadPoolPublishModel> threadPools = new HashMap<>();

  public ConsumerPublishModel getConsumer() {
    return consumer;
  }

  public void setConsumer(ConsumerPublishModel consumer) {
    this.consumer = consumer;
  }

  public ProducerPublishModel getProducer() {
    return producer;
  }

  public EdgePublishModel getEdge() {
    return edge;
  }

  public void setEdge(EdgePublishModel edge) {
    this.edge = edge;
  }

  public void setProducer(ProducerPublishModel producer) {
    this.producer = producer;
  }

  public Map<String, ThreadPoolPublishModel> getThreadPools() {
    return threadPools;
  }

  public void setThreadPools(Map<String, ThreadPoolPublishModel> threadPools) {
    this.threadPools = threadPools;
  }
}
