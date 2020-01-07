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

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersion;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersions;
import org.apache.servicecomb.swagger.engine.SwaggerProducer;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;

public final class CoreMetaUtils {
  public static final String CORE_MICROSERVICE_VERSIONS_META = "scb_microservice_versions_meta";

  public static final String CORE_MICROSERVICE_META = "scb_microservice_meta";

  public static final String CORE_MICROSERVICE_VERSION = "scb_microservice_version";

  public static final String SWAGGER_PRODUCER = "scb_swagger-producer";

  private CoreMetaUtils() {
  }

  public static <T extends MicroserviceVersionsMeta> T getMicroserviceVersionsMeta(
      MicroserviceVersions microserviceVersions) {
    return microserviceVersions.getVendorExtensions().get(CORE_MICROSERVICE_VERSIONS_META);
  }

  // only for consumer flow
  public static MicroserviceVersions getMicroserviceVersions(Invocation invocation) {
    return getMicroserviceVersions(invocation.getMicroserviceMeta());
  }

  // only for consumer flow
  public static MicroserviceVersions getMicroserviceVersions(MicroserviceMeta microserviceMeta) {
    return getMicroserviceVersion(microserviceMeta).getMicroserviceVersions();
  }

  // only for consumer flow
  public static MicroserviceVersion getMicroserviceVersion(MicroserviceMeta microserviceMeta) {
    return microserviceMeta.getExtData(CORE_MICROSERVICE_VERSION);
  }

  public static MicroserviceMeta getMicroserviceMeta(MicroserviceVersion microserviceVersion) {
    return microserviceVersion.getVendorExtensions().get(CORE_MICROSERVICE_META);
  }

  public static SwaggerProducer getSwaggerProducer(SchemaMeta schemaMeta) {
    return schemaMeta.getExtData(SWAGGER_PRODUCER);
  }
}
