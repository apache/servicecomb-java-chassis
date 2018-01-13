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

package org.apache.servicecomb.core.provider.consumer;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.CseContext;
import org.springframework.stereotype.Component;

@Component
public class ReferenceConfigUtils {
  private static boolean ready;

  public static void setReady(boolean ready) {
    ReferenceConfigUtils.ready = ready;
  }

  private static void assertIsReady() {
    if (!ready) {
      throw new IllegalStateException("System is not ready for remote calls. "
          + "When beans are making remote calls in initialization, it's better to "
          + "implement " + BootListener.class.getName() + " and do it after EventType.AFTER_REGISTRY.");
    }
  }

  public static ReferenceConfig getForInvoke(String microserviceName) {
    assertIsReady();

    return CseContext.getInstance().getConsumerProviderManager().getReferenceConfig(microserviceName);
  }

  public static ReferenceConfig getForInvoke(String microserviceName, String microserviceVersion,
      String transport) {
    assertIsReady();

    return CseContext.getInstance().getConsumerProviderManager().createReferenceConfig(microserviceName,
        microserviceVersion,
        transport);
  }
}
