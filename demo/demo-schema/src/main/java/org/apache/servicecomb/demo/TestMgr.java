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

package org.apache.servicecomb.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMgr {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestMgr.class);

  private static final List<Throwable> errorList = new ArrayList<>();

  private static String msg = "";

  private static final AtomicLong checkes = new AtomicLong(0);

  public static void setMsg(String msg) {
    TestMgr.msg = msg;
  }

  public static void setMsg(String microserviceName, String transport) {
    TestMgr.msg = String.format("microservice=%s, transport=%s", microserviceName, transport);
  }

  public static void check(Object expect, Object real) {
    check(expect, real, null);
  }

  public static void check(Object expect, Object real, Throwable error) {
    checkes.incrementAndGet();

    if (expect == real) {
      return;
    }

    String strExpect = String.valueOf(expect);
    String strReal = String.valueOf(real);

    if (!strExpect.equals(strReal)) {
      Error newError = new Error(msg + " | Expect " + strExpect + ", but " + strReal);
      if (error != null) {
        newError.setStackTrace(error.getStackTrace());
      }
      errorList.add(newError);
    }
  }

  public static void checkNotEmpty(String real) {
    checkes.incrementAndGet();

    if (StringUtils.isEmpty(real)) {
      errorList.add(new Error(msg + " | unexpected null result, method is " + getCaller()));
    }
  }

  public static void fail(String desc) {
    failed(desc, new Exception(desc));
  }

  public static void failed(String desc, Throwable e) {
    checkes.incrementAndGet();

    Error error = new Error(msg + " | " + desc + ", method is " + getCaller());
    if (e != null) {
      error.setStackTrace(error.getStackTrace());
    }
    errorList.add(error);
  }

  public static boolean isSuccess() {
    return errorList.isEmpty();
  }

  public static void summary() {
    if (errorList.isEmpty()) {
      LOGGER.info("............. test finished ............");
      LOGGER.info("............. total checks : " + checkes.get());
      return;
    }

    LOGGER.info("............. test not finished ............");
    LOGGER.info("............. total checks : " + checkes.get());
    LOGGER.info("............. total errors : " + errorList.size());
    LOGGER.info("............. error details: ");
    for (Throwable e : errorList) {
      LOGGER.info("", e);
    }
  }

  public static List<Throwable> errors() {
    return errorList;
  }

  private static String getCaller() {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    if (stackTrace.length < 3) {
      return null;
    }
    StackTraceElement stackTraceElement = stackTrace[3];
    return stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName();
  }
}
