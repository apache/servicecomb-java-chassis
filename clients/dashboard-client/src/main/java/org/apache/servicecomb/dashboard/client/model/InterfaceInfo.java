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

package org.apache.servicecomb.dashboard.client.model;

public class InterfaceInfo {
  private String name;

  private String desc;

  private double qps;

  private int latency;

  private int l995;

  private int l99;

  private int l90;

  private int l75;

  private int l50;

  private int l25;

  private int l5;

  private double rate;

  private double failureRate;

  private long total;

  private boolean isCircuitBreakerOpen;

  private long failure;

  private long shortCircuited;

  private long semaphoreRejected;

  private long threadPoolRejected;

  private long countTimeout;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public double getQps() {
    return qps;
  }

  public void setQps(double qps) {
    this.qps = qps;
  }

  public int getLatency() {
    return latency;
  }

  public void setLatency(int latency) {
    this.latency = latency;
  }

  public int getL995() {
    return l995;
  }

  public void setL995(int l995) {
    this.l995 = l995;
  }

  public int getL99() {
    return l99;
  }

  public void setL99(int l99) {
    this.l99 = l99;
  }

  public int getL90() {
    return l90;
  }

  public void setL90(int l90) {
    this.l90 = l90;
  }

  public int getL75() {
    return l75;
  }

  public void setL75(int l75) {
    this.l75 = l75;
  }

  public int getL50() {
    return l50;
  }

  public void setL50(int l50) {
    this.l50 = l50;
  }

  public int getL25() {
    return l25;
  }

  public void setL25(int l25) {
    this.l25 = l25;
  }

  public int getL5() {
    return l5;
  }

  public void setL5(int l5) {
    this.l5 = l5;
  }

  public double getRate() {
    return rate;
  }

  public void setRate(double rate) {
    this.rate = rate;
  }

  public double getFailureRate() {
    return failureRate;
  }

  public void setFailureRate(double failureRate) {
    this.failureRate = failureRate;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public boolean isCircuitBreakerOpen() {
    return isCircuitBreakerOpen;
  }

  public void setCircuitBreakerOpen(boolean circuitBreakerOpen) {
    isCircuitBreakerOpen = circuitBreakerOpen;
  }

  public long getFailure() {
    return failure;
  }

  public void setFailure(long failure) {
    this.failure = failure;
  }

  public long getShortCircuited() {
    return shortCircuited;
  }

  public void setShortCircuited(long shortCircuited) {
    this.shortCircuited = shortCircuited;
  }

  public long getSemaphoreRejected() {
    return semaphoreRejected;
  }

  public void setSemaphoreRejected(long semaphoreRejected) {
    this.semaphoreRejected = semaphoreRejected;
  }

  public long getThreadPoolRejected() {
    return threadPoolRejected;
  }

  public void setThreadPoolRejected(long threadPoolRejected) {
    this.threadPoolRejected = threadPoolRejected;
  }

  public long getCountTimeout() {
    return countTimeout;
  }

  public void setCountTimeout(long countTimeout) {
    this.countTimeout = countTimeout;
  }
}
