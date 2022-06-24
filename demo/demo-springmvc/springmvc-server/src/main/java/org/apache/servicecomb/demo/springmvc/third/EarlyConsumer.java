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

package org.apache.servicecomb.demo.springmvc.third;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * see: https://github.com/apache/servicecomb-java-chassis/issues/2534
 */
@Component
public class EarlyConsumer implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(EarlyConsumer.class);

  @RpcReference(microserviceName = "third", schemaId = "heartbeat")
  private HealthSchema healthSchema;

  private volatile boolean stopped = false;

  public EarlyConsumer() {
    new Thread(() -> {
      while (!stopped) {
        LOGGER.info("calling service");
        try {
          healthSchema.heartbeat();
        } catch (Throwable e) {
          // ignore error
        }
        try {
          Thread.sleep(100);
        } catch (Throwable e) {
          // ignore error
        }
      }
    }).start();
  }

  @Override
  public void onAfterRegistry(BootEvent event) {
    stopped = true;
  }
}
