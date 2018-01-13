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

package org.apache.servicecomb.core.definition.schema;

import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;

public class SchemaContext {
  protected MicroserviceMeta microserviceMeta;

  protected String schemaId;

  protected SchemaMeta schemaMeta;

  // consumer或producer对应的class
  protected Class<?> providerClass;

  public String getMicroserviceName() {
    return microserviceMeta.getName();
  }

  public MicroserviceMeta getMicroserviceMeta() {
    return microserviceMeta;
  }

  public void setMicroserviceMeta(MicroserviceMeta microserviceMeta) {
    this.microserviceMeta = microserviceMeta;
  }

  public String getSchemaId() {
    return schemaId;
  }

  public void setSchemaId(String schemaId) {
    this.schemaId = schemaId;
  }

  public SchemaMeta getSchemaMeta() {
    return schemaMeta;
  }

  public void setSchemaMeta(SchemaMeta schemaMeta) {
    this.schemaMeta = schemaMeta;
  }

  public Class<?> getProviderClass() {
    return providerClass;
  }

  public void setProviderClass(Class<?> providerClass) {
    this.providerClass = providerClass;
  }
}
