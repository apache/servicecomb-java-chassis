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

package org.apache.servicecomb.foundation.vertx;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import javax.xml.ws.Holder;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.ClientVerticle;
import org.apache.servicecomb.foundation.vertx.stream.BufferInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.FileResolver;
import io.vertx.core.impl.VertxImplEx;
import io.vertx.core.logging.SLF4JLogDelegateFactory;

/**
 * VertxUtils
 *
 *
 */
public final class VertxUtils {
  static {
    // initialize vertx logger, this can be done multiple times
    System.setProperty("vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
    io.vertx.core.logging.LoggerFactory.initialise();
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(VertxUtils.class);

  private static final long BLOCKED_THREAD_CHECK_INTERVAL = Long.MAX_VALUE / 2;

  // key为vertx实例名称，以支撑vertx功能分组
  private static Map<String, Vertx> vertxMap = new ConcurrentHashMap<>();

  private VertxUtils() {
  }

  public static <T extends AbstractVerticle> void deployVerticle(Vertx vertx, Class<T> cls, int instanceCount) {
    DeploymentOptions options = new DeploymentOptions().setInstances(instanceCount);

    vertx.deployVerticle(cls.getName(), options);
  }

  public static <CLIENT_POOL> DeploymentOptions createClientDeployOptions(
      ClientPoolManager<CLIENT_POOL> clientMgr,
      int instanceCount) {
    DeploymentOptions options = new DeploymentOptions().setInstances(instanceCount);
    SimpleJsonObject config = new SimpleJsonObject();
    config.put(ClientVerticle.CLIENT_MGR, clientMgr);
    options.setConfig(config);

    return options;
  }

  // deploy Verticle and wait for its success. do not call this method in event-loop thread
  public static <VERTICLE extends AbstractVerticle> boolean blockDeploy(Vertx vertx,
      Class<VERTICLE> cls,
      DeploymentOptions options) throws InterruptedException {
    Holder<Boolean> result = new Holder<>();

    CountDownLatch latch = new CountDownLatch(1);
    vertx.deployVerticle(cls.getName(), options, ar -> {
      result.value = ar.succeeded();

      if (ar.failed()) {
        LOGGER.error("deploy vertx failed, cause ", ar.cause());
      }

      latch.countDown();
    });

    latch.await();

    return result.value;
  }

  public static Vertx getOrCreateVertxByName(String name, VertxOptions vertxOptions) {
    Vertx vertx = getVertxByName(name);
    if (vertx == null) {
      synchronized (VertxUtils.class) {
        vertx = getVertxByName(name);
        if (vertx == null) {
          vertx = init(name, vertxOptions);
          vertxMap.put(name, vertx);
        }
      }
    }

    return vertx;
  }

  public static Vertx init(VertxOptions vertxOptions) {
    return init(null, vertxOptions);
  }

  public static Vertx init(String name, VertxOptions vertxOptions) {
    if (vertxOptions == null) {
      vertxOptions = new VertxOptions();
    }

    boolean isDebug = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;
    if (isDebug) {
      vertxOptions.setBlockedThreadCheckInterval(BLOCKED_THREAD_CHECK_INTERVAL);
      LOGGER.info("in debug mode, disable blocked thread check.");
    }

    configureVertxFileCaching();
    return new VertxImplEx(name, vertxOptions);
  }

  /**
   * 配置vertx的文件缓存功能，默认关闭
   */
  protected static void configureVertxFileCaching() {
    if (System.getProperty(FileResolver.DISABLE_CP_RESOLVING_PROP_NAME) == null) {
      System.setProperty(FileResolver.DISABLE_CP_RESOLVING_PROP_NAME, "true");
    }
  }

  public static Vertx currentVertx() {
    Context context = Vertx.currentContext();
    if (context == null) {
      throw new RuntimeException("get currentVertx error, currentContext is null.");
    }

    return context.owner();
  }

  public static Vertx getVertxByName(String name) {
    return vertxMap.get(name);
  }

  public static <T> void runInContext(Context context, AsyncResultCallback<T> callback, T result, Throwable e) {
    if (context == Vertx.currentContext()) {
      complete(callback, result, e);
    } else {
      context.runOnContext(v -> complete(callback, result, e));
    }
  }

  private static <T> void complete(AsyncResultCallback<T> callback, T result, Throwable e) {
    if (e != null) {
      callback.fail(e.getCause());
      return;
    }

    callback.success(result);
  }

  // try to reference byte[]
  // otherwise copy byte[]
  public static byte[] getBytesFast(InputStream inputStream) throws IOException {
    if (BufferInputStream.class.isInstance(inputStream)) {
      return getBytesFast(((BufferInputStream) inputStream).getByteBuf());
    }

    return IOUtils.toByteArray(inputStream);
  }

  public static byte[] getBytesFast(Buffer buffer) {
    ByteBuf byteBuf = buffer.getByteBuf();
    return getBytesFast(byteBuf);
  }

  public static byte[] getBytesFast(ByteBuf byteBuf) {
    if (byteBuf.hasArray()) {
      return byteBuf.array();
    }

    byte[] arr = new byte[byteBuf.writerIndex()];
    byteBuf.getBytes(0, arr);
    return arr;
  }
}
