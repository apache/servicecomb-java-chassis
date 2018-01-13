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

package org.apache.servicecomb.core;

import javax.inject.Inject;

import org.apache.servicecomb.core.definition.MicroserviceMetaManager;
import org.apache.servicecomb.core.definition.loader.SchemaListenerManager;
import org.apache.servicecomb.core.definition.loader.SchemaLoader;
import org.apache.servicecomb.core.definition.schema.ConsumerSchemaFactory;
import org.apache.servicecomb.core.provider.consumer.ConsumerProviderManager;
import org.apache.servicecomb.core.provider.producer.ProducerProviderManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;

public class CseContext {
  private static final CseContext INSTANCE = new CseContext();

  public static CseContext getInstance() {
    return INSTANCE;
  }

  private SchemaListenerManager schemaListenerManager;

  private SchemaLoader schemaLoader;

  private MicroserviceMetaManager microserviceMetaManager;

  private ConsumerSchemaFactory consumerSchemaFactory;

  private ConsumerProviderManager consumerProviderManager;

  private ProducerProviderManager producerProviderManager;

  private TransportManager transportManager;

  private SwaggerEnvironment swaggerEnvironment;

  public SchemaListenerManager getSchemaListenerManager() {
    return schemaListenerManager;
  }

  public SchemaLoader getSchemaLoader() {
    return schemaLoader;
  }

  public MicroserviceMetaManager getMicroserviceMetaManager() {
    return microserviceMetaManager;
  }

  public ConsumerSchemaFactory getConsumerSchemaFactory() {
    return consumerSchemaFactory;
  }

  public ConsumerProviderManager getConsumerProviderManager() {
    return consumerProviderManager;
  }

  public ProducerProviderManager getProducerProviderManager() {
    return producerProviderManager;
  }

  public TransportManager getTransportManager() {
    return transportManager;
  }

  public SwaggerEnvironment getSwaggerEnvironment() {
    return swaggerEnvironment;
  }

  @Inject
  public void setSwaggerEnvironment(SwaggerEnvironment swaggerEnvironment) {
    this.swaggerEnvironment = swaggerEnvironment;
  }

  @Inject
  public void setMicroserviceMetaManager(MicroserviceMetaManager microserviceMetaManager) {
    this.microserviceMetaManager = microserviceMetaManager;
  }

  @Inject
  public void setSchemaLoader(SchemaLoader schemaLoader) {
    this.schemaLoader = schemaLoader;
  }

  @Inject
  public void setConsumerSchemaFactory(ConsumerSchemaFactory consumerSchemaFactory) {
    this.consumerSchemaFactory = consumerSchemaFactory;
  }

  @Inject
  public void setConsumerProviderManager(ConsumerProviderManager consumerProviderManager) {
    this.consumerProviderManager = consumerProviderManager;
  }

  @Inject
  public void setProducerProviderManager(ProducerProviderManager producerProviderManager) {
    this.producerProviderManager = producerProviderManager;
  }

  @Inject
  public void setSchemaListenerManager(SchemaListenerManager schemaListenerManager) {
    this.schemaListenerManager = schemaListenerManager;
  }

  @Inject
  public void setTransportManager(TransportManager transportManager) {
    this.transportManager = transportManager;
  }
}
