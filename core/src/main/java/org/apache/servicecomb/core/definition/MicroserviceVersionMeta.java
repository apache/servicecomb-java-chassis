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

package org.apache.servicecomb.core.definition;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.definition.classloader.MicroserviceClassLoaderFactory;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersion;

public class MicroserviceVersionMeta extends MicroserviceVersion {
  MicroserviceMeta microserviceMeta;

  MicroserviceVersionMeta(Microservice microservice) {
    super(microservice);
  }

  public MicroserviceVersionMeta(String microserviceName, String microserviceId,
      MicroserviceClassLoaderFactory classLoaderFactory) {
    super(microserviceId);

    this.microserviceMeta = new MicroserviceMeta(microserviceName);
    this.microserviceMeta.setClassLoader(
        classLoaderFactory.create(microservice.getAppId(), microserviceName, microservice.getVersion()));
    if (Const.REGISTRY_APP_ID.equals(microservice.getAppId()) && Const.REGISTRY_SERVICE_NAME.equals(microserviceName)) {
      // do not load service center schemas
      return;
    }

    CseContext.getInstance().getConsumerSchemaFactory().createConsumerSchema(microserviceMeta, microservice);
    CseContext.getInstance().getSchemaListenerManager().notifySchemaListener(microserviceMeta);
  }

  public MicroserviceMeta getMicroserviceMeta() {
    return microserviceMeta;
  }
}
