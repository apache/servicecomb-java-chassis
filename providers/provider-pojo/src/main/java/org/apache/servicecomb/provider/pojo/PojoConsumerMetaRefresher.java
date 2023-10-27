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
package org.apache.servicecomb.provider.pojo;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.provider.consumer.MicroserviceReferenceConfig;
import org.apache.servicecomb.provider.pojo.definition.PojoConsumerMeta;
import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Response.Status;

public class PojoConsumerMetaRefresher {
  private static final Logger LOGGER = LoggerFactory.getLogger(PojoConsumerMetaRefresher.class);

  protected final String microserviceName;

  // can be null, should find SchemaMeta by consumerIntf in this time
  protected final String schemaId;

  protected final Class<?> consumerIntf;

  protected SCBEngine scbEngine;

  // not always equals codec meta
  // for highway, codec meta is relate to target instance
  //  to avoid limit producer to only allow append parameter
  protected PojoConsumerMeta consumerMeta;

  public PojoConsumerMetaRefresher(String microserviceName, String schemaId, Class<?> consumerIntf) {
    this.microserviceName = microserviceName;
    this.schemaId = schemaId;
    this.consumerIntf = consumerIntf;
  }

  public PojoConsumerMeta getLatestMeta() {
    ensureStatusUp();
    ensureMetaAvailable();
    return consumerMeta;
  }

  public CompletableFuture<PojoConsumerMeta> getLatestMetaAsync() {
    ensureStatusUp();
    return ensureMetaAvailableAsync().thenCompose(v -> CompletableFuture.completedFuture(consumerMeta));
  }

  private void ensureStatusUp() {
    if (scbEngine == null) {
      if (SCBEngine.getInstance() == null) {
        String message =
            "The request is rejected. Cannot process the request due to SCBEngine not ready.";
        LOGGER.warn(message);
        throw new InvocationException(Status.SERVICE_UNAVAILABLE, new CommonExceptionData(message));
      }

      this.scbEngine = SCBEngine.getInstance();
    }

    scbEngine.ensureStatusUp();
  }

  private void ensureMetaAvailable() {
    if (isNeedRefresh()) {
      synchronized (this) {
        if (isNeedRefresh()) {
          this.consumerMeta = refreshMeta();
        }
      }
    }
  }

  private CompletableFuture<Void> ensureMetaAvailableAsync() {
    if (isNeedRefresh()) {
      return refreshMetaAsync();
    }
    return CompletableFuture.completedFuture(null);
  }

  private boolean isNeedRefresh() {
    return consumerMeta == null;
  }

  protected PojoConsumerMeta refreshMeta() {
    MicroserviceReferenceConfig microserviceReferenceConfig = scbEngine
        .getOrCreateReferenceConfig(microserviceName);
    if (microserviceReferenceConfig == null) {
      throw new InvocationException(Status.INTERNAL_SERVER_ERROR,
          new CommonExceptionData(String.format("Failed to invoke service %s. Maybe service"
              + " not registered or no active instance.", microserviceName)));
    }
    MicroserviceMeta microserviceMeta = microserviceReferenceConfig.getMicroserviceMeta();

    SchemaMeta schemaMeta = findSchemaMeta(microserviceMeta);
    if (schemaMeta == null) {
      throw new IllegalStateException(
          String.format(
              "Schema not exist, microserviceName=%s, schemaId=%s, consumer interface=%s; "
                  + "new producer not running or not deployed.",
              microserviceName,
              schemaId,
              consumerIntf.getName()));
    }

    SwaggerConsumer swaggerConsumer = scbEngine.getSwaggerEnvironment()
        .createConsumer(consumerIntf, schemaMeta.getSwagger());
    return new PojoConsumerMeta(microserviceReferenceConfig, swaggerConsumer, schemaMeta);
  }

  protected CompletableFuture<Void> refreshMetaAsync() {
    CompletableFuture<MicroserviceReferenceConfig> microserviceReferenceConfigFuture = scbEngine
        .getOrCreateReferenceConfigAsync(microserviceName);
    return microserviceReferenceConfigFuture.thenCompose((microserviceReferenceConfig) -> {
      if (microserviceReferenceConfig == null) {
        return CompletableFuture.failedFuture(new InvocationException(Status.INTERNAL_SERVER_ERROR,
            new CommonExceptionData(String.format("Failed to invoke service %s. Maybe service"
                + " not registered or no active instance.", microserviceName))));
      }
      synchronized (this) {
        if (isNeedRefresh()) {
          MicroserviceMeta microserviceMeta = microserviceReferenceConfig.getMicroserviceMeta();

          SchemaMeta schemaMeta = findSchemaMeta(microserviceMeta);
          if (schemaMeta == null) {
            return CompletableFuture.failedFuture(new IllegalStateException(
                String.format(
                    "Schema not exist, microserviceName=%s, schemaId=%s, consumer interface=%s; "
                        + "new producer not running or not deployed.",
                    microserviceName,
                    schemaId,
                    consumerIntf.getName())));
          }

          SwaggerConsumer swaggerConsumer = scbEngine.getSwaggerEnvironment()
              .createConsumer(consumerIntf, schemaMeta.getSwagger());
          consumerMeta = new PojoConsumerMeta(microserviceReferenceConfig, swaggerConsumer, schemaMeta);
        }
        return CompletableFuture.completedFuture(null);
      }
    });
  }

  private SchemaMeta findSchemaMeta(MicroserviceMeta microserviceMeta) {
    // if present schemaId, just use it
    if (StringUtils.isNotEmpty(schemaId)) {
      return microserviceMeta.findSchemaMeta(schemaId);
    }

    // try interface name second
    return microserviceMeta.findSchemaMeta(consumerIntf.getName());
  }
}
