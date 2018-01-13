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

package org.apache.servicecomb.foundation.metrics.performance;

import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

/**
 * Implements the collection of metrics such as total request, total fail
 * request and average times for requests.
 */
public class MetricsDataMonitor {

  // inc
  // invocation start
  // succ + fail
  public Long totalReqProvider = 0L;

  // inc
  // after invocation finished
  public Long totalFailReqProvider = 0L;

  // inc
  // after invocation start
  public Long totalReqConsumer = 0L;

  // inc
  // after invocation finished
  public Long totalFailReqConsumer = 0L;

  // key is operQualifiedName
  // inc
  // after invocation finished
  public Map<String, Long> operMetricsTotalReq = new ConcurrentHashMapEx<>();

  // key is operQualifiedName
  // inc
  // after invocation finished
  public Map<String, Long> operMetricsTotalFailReq = new ConcurrentHashMapEx<>();

  // key is operQualifiedName
  public Map<String, QueueMetricsData> queueMetrics = new ConcurrentHashMapEx<>();

  /**
   * default constructor.
   */
  public MetricsDataMonitor() {

  }

  /**
   * Returns the map of average values for both instance and operational level.
   * 
   * @param pathId Operation path id
   * @return QueueMetrics object based on key
   */
  public QueueMetricsData getOrCreateQueueMetrics(String pathId) {
    return queueMetrics.computeIfAbsent(pathId, p -> new QueueMetricsData());
  }

  /**
   * Returns the map of average values for both instance and operational level.
   * 
   * @return queue metrics map
   */
  public Map<String, QueueMetricsData> getQueueMetrics() {
    return queueMetrics;
  }

  /**
   * Returns the map of average values for both instance and operational level.
   * 
   * @param newMap the new map which passed to queue metrics
   */
  public void setQueueMetrics(Map<String, QueueMetricsData> newMap) {
    queueMetrics = newMap;
  }

  /**
   * Sets the map of average values for both instance and operational levels.
   * 
   * @param pathId Operation path id
   * @param reqQueue RequestQueue
   */
  public void setQueueMetrics(String pathId, QueueMetricsData reqQueue) {
    this.queueMetrics.put(pathId, reqQueue);
  }

  /**
   * Returns the total requests per provider.
   * 
   * @return long total requests for provider
   */
  public long getTotalReqProvider() {
    return totalReqProvider;
  }

  /**
   * Increments the total requests per provider.
   */
  public void incrementTotalReqProvider() {
    this.totalReqProvider++;
  }

  /**
   * Sets the total requests per provider.
   * @param totalReqProvider the total requests per provider
   */
  public void setTotalReqProvider(Long totalReqProvider) {
    this.totalReqProvider = totalReqProvider;
  }

  /**
   * Returns the total fail requests per provider.
   * 
   * @return long total failed requests for provider
   */
  public long getTotalFailReqProvider() {
    return totalFailReqProvider;
  }

  /**
   * Sets the total fail requests per provider.
   */
  public void incrementTotalFailReqProvider() {
    this.totalFailReqProvider++;
  }

  /**
   * Sets the total failed requests per provider.
   * @param totalFailedReqProvider the total failed requests per provider
   */
  public void setTotalFailReqProvider(Long totalFailedReqProvider) {
    this.totalFailReqProvider = totalFailedReqProvider;
  }

  /**
   * Returns the total requests per consumer.
   * 
   * @return long total requests for consumer
   */
  public long getTotalReqConsumer() {
    return totalReqConsumer;
  }

  /**
   * Sets the total requests per consumer.
   */
  public void incrementTotalReqConsumer() {
    this.totalReqConsumer++;
  }

  /**
   * Sets the total failed requests per consumer.
   * @param totalReqConsumer the total requests per consumer
   */
  public void setTotalReqConsumer(Long totalReqConsumer) {
    this.totalReqConsumer = totalReqConsumer;
  }

  /**
   * Returns the total fail requests per consumer.
   * 
   * @return long total failed request for consumer
   */
  public long getTotalFailReqConsumer() {
    return totalFailReqConsumer;
  }

  /**
   * Sets the total fail requests per consumer.
   */
  public void incrementTotalFailReqConsumer() {
    this.totalFailReqConsumer++;
  }

  /**
   * Sets the total failed requests per consumer.
   * @param totalFailedReqConsumer the total failed requests per consumer
   */
  public void setTotalFailReqConsumer(Long totalFailedReqConsumer) {
    this.totalFailReqConsumer = totalFailedReqConsumer;
  }

  /**
   * Returns total requests per provider for operational level.
   * 
   * @param key Operation path id
   * @return long total requests per provider
   */
  public Long getOperMetTotalReq(String key) {
    return operMetricsTotalReq.get(key);
  }

  /**
   * Sets total requests per provider for operational level.
   * 
   * @param key pathId
   * @param val total requests per provider
   */
  public void setOperMetTotalReq(String key, Long val) {
    this.operMetricsTotalReq.put(key, val);
  }

  /**
   * Returns total fail requests per provider for operational level.
   * 
   * @param key Operation path id
   * @return long total fail requests per provider
   */
  public Long getOperMetTotalFailReq(String key) {
    return operMetricsTotalFailReq.get(key);
  }

  /**
   * Sets total fail requests per provider for operational level.
   * 
   * @param key Operation path id
   * @param val total fail requests per provider
   */
  public void setOperMetTotalFailReq(String key, Long val) {
    this.operMetricsTotalFailReq.put(key, val);
  }
}
