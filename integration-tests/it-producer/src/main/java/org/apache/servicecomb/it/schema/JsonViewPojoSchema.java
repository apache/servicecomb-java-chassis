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
package org.apache.servicecomb.it.schema;

import org.apache.servicecomb.provider.pojo.RpcSchema;

import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.annotations.SwaggerDefinition;

@RpcSchema(schemaId = "jsonViewPojoSchema")
@SwaggerDefinition(basePath = "/v1/jsonViewPojoSchema")
public class JsonViewPojoSchema {
  public PersonViewModel jsonViewDefault() {
    return PersonViewModel.generatePersonViewModel();
  }

  @JsonView(PersonViewModel.Summary.class)
  public PersonViewModel jsonViewDefaultWithSummary() {
    return PersonViewModel.generatePersonViewModel();
  }

  @JsonView(PersonViewModel.SummaryWithDetails.class)
  public PersonViewModel jsonViewDefaultWithSummaryDetails() {
    return PersonViewModel.generatePersonViewModel();
  }

  public PersonViewModel jsonViewProducesDefault() {
    return PersonViewModel.generatePersonViewModel();
  }

  @JsonView(PersonViewModel.Summary.class)
  public PersonViewModel jsonViewProducesDefaultWithSummary() {
    return PersonViewModel.generatePersonViewModel();
  }

  @JsonView(PersonViewModel.SummaryWithDetails.class)
  public PersonViewModel jsonViewProducesDefaultWithSummaryDetails() {
    return PersonViewModel.generatePersonViewModel();
  }

  public String jsonViewPlainDefault() {
    return PersonViewModel.generatePersonViewModel().toString();
  }

  @JsonView(PersonViewModel.Summary.class)
  public String jsonViewPlainDefaultWithSummary() {
    return PersonViewModel.generatePersonViewModel().toString();
  }

  @JsonView(PersonViewModel.SummaryWithDetails.class)
  public String jsonViewPlainDefaultWithSummaryDetails() {
    return PersonViewModel.generatePersonViewModel().toString();
  }

  public PersonViewModel jsonViewPostDefault(PersonViewModel personViewModel) {
    return personViewModel;
  }

  public PersonViewModel jsonViewPostDefaultWithSummary(
      @JsonView(PersonViewModel.Summary.class) PersonViewModel personViewModel) {
    return personViewModel;
  }

  public PersonViewModel jsonViewPostDefaultWithSummaryDetails(
      @JsonView(PersonViewModel.SummaryWithDetails.class) PersonViewModel personViewModel) {
    return personViewModel;
  }
}
