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

package org.apache.servicecomb.governance.handler.ext;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import io.vertx.core.VertxException;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/*.xml", initializers = ConfigDataApplicationContextInitializer.class)
public class RetryExtensionTest {

  @Test
  public void test_status_code_to_contains() {
    List<String> statusList = Arrays.asList("502", "503");
    boolean result = AbstractRetryExtension.statusCodeContains(statusList, "502");
    Assertions.assertTrue(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "504");
    Assertions.assertFalse(result);

    statusList = Arrays.asList("5xx", "4x4", "4x", "x32", "xx6");
    result = AbstractRetryExtension.statusCodeContains(statusList, "502");
    Assertions.assertTrue(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "504");
    Assertions.assertTrue(result);

    statusList = Arrays.asList("4x4", "x32", "xx6");
    result = AbstractRetryExtension.statusCodeContains(statusList, "402");
    Assertions.assertFalse(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "404");
    Assertions.assertTrue(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "332");
    Assertions.assertTrue(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "446");
    Assertions.assertTrue(result);

    statusList = Arrays.asList("4x", "x3x", "x5");
    result = AbstractRetryExtension.statusCodeContains(statusList, "446");
    Assertions.assertFalse(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "455");
    Assertions.assertFalse(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "434");
    Assertions.assertTrue(result);
  }

  @Test
  public void testRetryWithConnectionException() {
    Exception target = new ConnectException("connection refused");
    Exception root = new Exception(target);
    boolean canRetry = FailurePredictor.canRetryForException(FailurePredictor.STRICT_RETRIABLE, root);
    Assertions.assertTrue(canRetry);
  }

  @Test
  public void testRetryWithSocketTimeout() {
    Exception target = new SocketTimeoutException("Read timed out");
    Exception root = new Exception(target);
    boolean canRetry = FailurePredictor.canRetryForException(FailurePredictor.STRICT_RETRIABLE, root);
    Assertions.assertTrue(canRetry);
  }

  @Test
  public void testRetryWithIOException() {
    Exception target = new IOException("Connection reset by peer");
    Exception root = new Exception(target);
    boolean canRetry = FailurePredictor.canRetryForException(FailurePredictor.STRICT_RETRIABLE, root);
    Assertions.assertTrue(canRetry);

    target = new IOException("Target not exist");
    root = new Exception(target);
    canRetry = FailurePredictor.canRetryForException(FailurePredictor.STRICT_RETRIABLE, root);
    Assertions.assertFalse(canRetry);
  }

  @Test
  public void testRetryVertxException() {
    Exception target = new VertxException("Connection was closed");
    Exception root = new Exception(target);
    boolean canRetry = FailurePredictor.canRetryForException(FailurePredictor.STRICT_RETRIABLE, root);
    Assertions.assertTrue(canRetry);

    target = new IOException("");
    root = new Exception(target);
    canRetry = FailurePredictor.canRetryForException(FailurePredictor.STRICT_RETRIABLE, root);
    Assertions.assertFalse(canRetry);
  }

  @Test
  public void testRetryNoRouteToHostException() {
    Exception target = new NoRouteToHostException("Host is unreachable");
    Exception root = new Exception(target);
    boolean canRetry = FailurePredictor.canRetryForException(FailurePredictor.STRICT_RETRIABLE, root);
    Assertions.assertTrue(canRetry);

    target = new NoRouteToHostException("No route to host");
    root = new Exception(target);
    canRetry = FailurePredictor.canRetryForException(FailurePredictor.STRICT_RETRIABLE, root);
    Assertions.assertTrue(canRetry);
  }

  @Test
  public void testRetryEqualTen() {
    Exception target = new ConnectException("connectin refused");
    for (int i = 0; i < 8; i++) {
      target = new Exception("Level" + i, target);
    }
    Exception root = new Exception(target);
    boolean canRetry = FailurePredictor.canRetryForException(FailurePredictor.STRICT_RETRIABLE, root);
    Assertions.assertTrue(canRetry);
  }

  @Test
  public void testRetryOverTen() {
    Exception target = new ConnectException("connectin refused");
    for (int i = 0; i < 9; i++) {
      target = new Exception("Level" + i, target);
    }
    Exception root = new Exception(target);
    boolean canRetry = FailurePredictor.canRetryForException(FailurePredictor.STRICT_RETRIABLE, root);
    Assertions.assertFalse(canRetry);
  }
}
