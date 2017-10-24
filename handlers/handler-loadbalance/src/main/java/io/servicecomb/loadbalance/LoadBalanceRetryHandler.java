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

package io.servicecomb.loadbalance;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

import com.google.common.collect.Lists;
import com.netflix.client.RetryHandler;
import com.netflix.client.Utils;

public class LoadBalanceRetryHandler implements RetryHandler {

  private final static LoadBalanceRetryHandler INSTANCE = new LoadBalanceRetryHandler();

  private int retrySameServer;
  private int retryNextServer;
  private boolean retryEnabled;

  public static LoadBalanceRetryHandler getInstance() {
    return INSTANCE;
  }

  private List<Class<? extends Throwable>> retryOnSameExceptions = Lists
      .newArrayList(new Class[]{SocketTimeoutException.class, ConnectException.class});

  private List<Class<? extends Throwable>> circuitRelated = Lists
      .newArrayList(new Class[]{SocketException.class, SocketTimeoutException.class});

  private LoadBalanceRetryHandler() {
    this.retrySameServer = 0;
    this.retryNextServer = 0;
    this.retryEnabled = true;
  }

  public void setRetryRuntimeParams(int retrySameServer, int retryNextServer, boolean retryEnabled) {
    this.retrySameServer = retrySameServer;
    this.retryNextServer = retryNextServer;
    this.retryEnabled = retryEnabled;
  }

  public void addRetryOnSameExceptions(List<Class<? extends Throwable>> retryOnSameExceptions) {
    if (retryOnSameExceptions != null && retryOnSameExceptions.size() != 0) {
      this.retryOnSameExceptions.addAll(retryOnSameExceptions);
    }
  }

  public List<Class<? extends Throwable>> getRetryOnSameExceptions() {
    return retryOnSameExceptions;
  }

  public boolean removeRetryOnSameException(Class<? extends Throwable> exception) {
    return retryOnSameExceptions.remove(exception);
  }

  @Override
  public boolean isRetriableException(Throwable e, boolean sameServer) {
    return retryEnabled && (!sameServer || Utils.isPresentAsCause(e, retryOnSameExceptions));
  }

  @Override
  public boolean isCircuitTrippingException(Throwable e) {
    return Utils.isPresentAsCause(e, circuitRelated);
  }

  @Override
  public int getMaxRetriesOnSameServer() {
    return retrySameServer;
  }

  @Override
  public int getMaxRetriesOnNextServer() {
    return retryNextServer;
  }


}
