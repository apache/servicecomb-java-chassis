/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.core.definition;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.definition.classloader.MicroserviceClassLoaderFactory;
import io.servicecomb.serviceregistry.consumer.MicroserviceVersion;

public class MicroserviceVersionMeta extends MicroserviceVersion {
  private MicroserviceMeta microserviceMeta;

  public MicroserviceVersionMeta(String microserviceName, String microserviceId,
      MicroserviceClassLoaderFactory classLoaderFactory) {
    super(microserviceId);

    this.microserviceMeta = new MicroserviceMeta(microserviceName);
    this.microserviceMeta.setClassLoader(classLoaderFactory.create(microserviceName, microservice.getVersion()));
    CseContext.getInstance().getConsumerSchemaFactory().getOrCreateConsumerSchema(microserviceMeta, microservice);
    CseContext.getInstance().getSchemaListenerManager().notifySchemaListener(microserviceMeta);
  }

  public MicroserviceMeta getMicroserviceMeta() {
    return microserviceMeta;
  }
}
