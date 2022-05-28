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
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * see: https://github.com/apache/servicecomb-java-chassis/issues/2534
 */
@Component
public class NormalConsumer implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(NormalConsumer.class);

  @RpcReference(microserviceName = "third", schemaId = "heartbeat")
  private HealthSchema healthSchema;

  @Override
  public void onAfterRegistry(BootListener.BootEvent event) {
    try {
      LOGGER.info("calling service after register");

      healthSchema.heartbeat();
      LOGGER.info("heartbeat succ");
    } catch (Throwable e) {
      TestMgr.failed("3rd invoke failed", e);
      throw e;
    }
  }


  @Override
  public int getOrder() {
    // 比ThirdServiceRegister晚
    return 0;
  }
}
