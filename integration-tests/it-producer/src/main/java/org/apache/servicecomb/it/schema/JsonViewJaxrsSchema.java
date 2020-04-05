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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.annotation.JsonView;

@RestSchema(schemaId = "jsonViewJaxrsSchema")
@Path("/v1/jsonViewJaxrsSchema")
public class JsonViewJaxrsSchema {

  @GET
  @Path("jsonViewDefault")
  public PersonViewModel jsonViewDefault() {
    return PersonViewModel.generatePersonViewModel();
  }

  @GET
  @Path("jsonViewDefaultWithSummary")
  @JsonView(PersonViewModel.Summary.class)
  public PersonViewModel jsonViewDefaultWithSummary() {
    return PersonViewModel.generatePersonViewModel();
  }

  @GET
  @Path("jsonViewDefaultWithSummaryDetails")
  @JsonView(PersonViewModel.SummaryWithDetails.class)
  public PersonViewModel jsonViewDefaultWithSummaryDetails() {
    return PersonViewModel.generatePersonViewModel();
  }

  @GET
  @Path("jsonViewProducesDefault")
  @Produces("application/json")
  public PersonViewModel jsonViewProducesDefault() {
    return PersonViewModel.generatePersonViewModel();
  }

  @GET
  @Path("jsonViewProducesDefaultWithSummary")
  @Produces("application/json")
  @JsonView(PersonViewModel.Summary.class)
  public PersonViewModel jsonViewProducesDefaultWithSummary() {
    return PersonViewModel.generatePersonViewModel();
  }

  @GET
  @Path("jsonViewProducesDefaultWithSummaryDetails")
  @Produces("application/json")
  @JsonView(PersonViewModel.SummaryWithDetails.class)
  public PersonViewModel jsonViewProducesDefaultWithSummaryDetails() {
    return PersonViewModel.generatePersonViewModel();
  }

  @GET
  @Path("jsonViewPlainDefault")
  @Produces("text/plain")
  public String jsonViewPlainDefault() {
    return PersonViewModel.generatePersonViewModel().toString();
  }

  @GET
  @Path("jsonViewPlainDefaultWithSummary")
  @Produces("text/plain")
  @JsonView(PersonViewModel.Summary.class)
  public String jsonViewPlainDefaultWithSummary() {
    return PersonViewModel.generatePersonViewModel().toString();
  }

  @GET
  @Path("jsonViewPlainDefaultWithSummaryDetails")
  @Produces("text/plain")
  @JsonView(PersonViewModel.SummaryWithDetails.class)
  public String jsonViewPlainDefaultWithSummaryDetails() {
    return PersonViewModel.generatePersonViewModel().toString();
  }

  @POST
  @Path("jsonViewPostDefault")
  public PersonViewModel jsonViewPostDefault(@RequestBody PersonViewModel personViewModel) {
    return personViewModel;
  }

  @POST
  @Path("jsonViewPostDefaultWithSummary")
  public PersonViewModel jsonViewPostDefaultWithSummary(
      @JsonView(PersonViewModel.Summary.class) @RequestBody PersonViewModel personViewModel) {
    return personViewModel;
  }

  @POST
  @Path("jsonViewPostDefaultWithSummaryDetails")
  public PersonViewModel jsonViewPostDefaultWithSummaryDetails(
      @JsonView(PersonViewModel.SummaryWithDetails.class) @RequestBody PersonViewModel personViewModel) {
    return personViewModel;
  }
}
