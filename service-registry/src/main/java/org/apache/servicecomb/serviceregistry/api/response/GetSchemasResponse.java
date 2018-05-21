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

package org.apache.servicecomb.serviceregistry.api.response;

import java.util.List;

public class GetSchemasResponse {

  //to compatible service center interface, cur version return schema, but next version will change to schemas
  private List<GetSchemaResponse> schema;

  private List<GetSchemaResponse> schemas;

  public List<GetSchemaResponse> getSchema() {
    return schema;
  }

  public void setSchema(List<GetSchemaResponse> schema) {
    this.schema = schema;
  }

  public List<GetSchemaResponse> getSchemas() {
    return schemas;
  }

  public void setSchemas(List<GetSchemaResponse> schemas) {
    this.schemas = schemas;
  }
}
