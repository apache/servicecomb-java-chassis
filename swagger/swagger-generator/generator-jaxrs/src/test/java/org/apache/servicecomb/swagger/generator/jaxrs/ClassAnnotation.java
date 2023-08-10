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
package org.apache.servicecomb.swagger.generator.jaxrs;

import org.apache.servicecomb.foundation.test.scaffolding.model.User;

import jakarta.servlet.http.Part;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/class")
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
@SuppressWarnings("unused")
public class ClassAnnotation {
  @Path(value = "testBean")
  @POST
  public User testBean(User user) {
    return null;
  }

  @Path(value = "testString")
  @POST
  public String testString(String user) {
    return null;
  }

  // This case should cause error. Must implicitly specify consumes.
  // For JAX RS, @FormParam can be url-encoded-form or multipart.
  @Path(value = "testFormWrong")
  @POST
  public String testFormWrong(@FormParam("param") int param) {
    return null;
  }

  // This case should cause error. Must implicitly specify consumes
  // For JAX RS, @FormParam can be url-encoded-form or multipart.
  @Path(value = "testUploadWrong")
  @POST
  public String testUploadWrong(@FormParam("part") Part part) {
    return null;
  }

  @Path(value = "testForm")
  @POST
  @Consumes(value = MediaType.APPLICATION_FORM_URLENCODED)
  public String testForm(@FormParam("param") int param) {
    return null;
  }

  @Path(value = "testUpload")
  @POST
  @Consumes(value = MediaType.MULTIPART_FORM_DATA)
  public String testUpload(@FormParam("part") Part part) {
    return null;
  }
}
