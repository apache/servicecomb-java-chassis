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

package org.apache.servicecomb.loadbalance;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.netflix.client.RetryHandler;

import io.vertx.core.VertxException;

public class TestDefaultRetryhandler {

  private static final String RETYR_NAME = "default";

  private static final String MICROSERVICE_NAME = "servicename";

  private RetryHandler retryHandler;

  @Before
  public void setup() {
    DefaultRetryExtensionsFactory factory = new DefaultRetryExtensionsFactory();
    retryHandler = factory.createRetryHandler(RETYR_NAME, MICROSERVICE_NAME);
  }

  @Test
  public void testRetryWithConnectionException() {
    Exception target = new ConnectException("connectin refused");
    Exception root = new Exception(target);
    boolean retriable = retryHandler.isRetriableException(root, false);
    Assert.assertTrue(retriable);
  }

  @Test
  public void testRetryWithSocketTimeout() {
    Exception target = new SocketTimeoutException("Read timed out");
    Exception root = new Exception(target);
    boolean retriable = retryHandler.isRetriableException(root, false);
    Assert.assertTrue(retriable);
  }

  @Test
  public void testRetryWithIOException() {
    Exception target = new IOException("Connection reset by peer");
    Exception root = new Exception(target);
    boolean retriable = retryHandler.isRetriableException(root, false);
    Assert.assertTrue(retriable);

    target = new IOException("Target not exist");
    root = new Exception(target);
    retriable = retryHandler.isRetriableException(root, false);
    Assert.assertFalse(retriable);
  }

  @Test
  public void testRetryVertxException() {
    Exception target = new VertxException("Connection was closed");
    Exception root = new Exception(target);
    boolean retriable = retryHandler.isRetriableException(root, false);
    Assert.assertTrue(retriable);

    target = new IOException("");
    root = new Exception(target);
    retriable = retryHandler.isRetriableException(root, false);
    Assert.assertFalse(retriable);
  }

  @Test
  public void testRetryInvocation503() {
    Exception root = new InvocationException(503, "Service Unavailable", "Error");
    boolean retriable = retryHandler.isRetriableException(root, false);
    Assert.assertTrue(retriable);
  }

  @Test
  public void testRetryEqualTen() {
    Exception target = new ConnectException("connectin refused");
    for (int i = 0; i < 8; i++) {
      target = new Exception("Level" + i, target);
    }
    Exception root = new Exception(target);
    boolean retriable = retryHandler.isRetriableException(root, false);
    Assert.assertTrue(retriable);
  }
  
  @Test
  public void testRetryOverTen() {
    Exception target = new ConnectException("connectin refused");
    for (int i = 0; i < 9; i++) {
      target = new Exception("Level" + i, target);
    }
    Exception root = new Exception(target);
    boolean retriable = retryHandler.isRetriableException(root, false);
    Assert.assertFalse(retriable);
  }
}
