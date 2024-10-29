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
package org.apache.servicecomb.registry.etcd;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuteExceptionUtil {

  interface FunctionWithException<T, R> {
    R apply(T t) throws Exception;
  }

  interface FunctionWithDoubleParam<T1, T2, R> {
    R apply(T1 t1, T2 t2) throws Exception;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(MuteExceptionUtil.class);

  public static class MuteExceptionUtilBuilder {

    private String logMessage;

    private Object[] customMessageParams;

    public MuteExceptionUtilBuilder withLog(String message, Object... params) {
      this.logMessage = message;
      this.customMessageParams = params;
      return this;
    }

    private String getLogMessage(String defaultMessage) {
      return logMessage != null ? logMessage : defaultMessage;
    }

    // 执行带异常处理的Function
    public <T, R> R executeFunction(FunctionWithException<T, R> function, T t) {
      try {
        return function.apply(t);
      } catch (Exception e) {
        LOGGER.error(getLogMessage("execute Function failure..."), customMessageParams, e);
        return null;
      }
    }

    public <T> T executeSupplier(Supplier<T> supplier) {
      try {
        return supplier.get();
      } catch (Exception e) {
        LOGGER.error(getLogMessage("execute Supplier failure..."), customMessageParams, e);
        return null;
      }
    }

    public <T> T executeCompletableFuture(CompletableFuture<T> completableFuture) {
      try {
        return completableFuture.get();
      } catch (Exception e) {
        LOGGER.error(getLogMessage("execute CompletableFuture failure..."), customMessageParams, e);
        return null;
      }
    }

    public <T1, T2, R> R executeFunctionWithDoubleParam(FunctionWithDoubleParam<T1, T2, R> function, T1 t1, T2 t2) {
      try {
        return function.apply(t1, t2);
      } catch (Exception e) {
        LOGGER.error(getLogMessage("execute FunctionWithDoubleParam failure..."), customMessageParams, e);
        return null;
      }
    }
  }

  public static MuteExceptionUtilBuilder builder() {
    return new MuteExceptionUtilBuilder();
  }
}
