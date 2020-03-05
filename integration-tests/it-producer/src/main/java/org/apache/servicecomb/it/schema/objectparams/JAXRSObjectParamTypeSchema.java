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

package org.apache.servicecomb.it.schema.objectparams;

import java.util.List;
import java.util.Map;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "JAXRSObjectParamTypeSchema")
@Path("/v1/JAXRSObjectParamTypeSchema")
public class JAXRSObjectParamTypeSchema implements ObjectParamTypeSchema {
  @Path("testFlattenObjectParam")
  @POST
  @Override
  public FlattenObjectResponse testFlattenObjectParam(FlattenObjectRequest request) {
    return RestObjectMapperFactory.getRestObjectMapper().convertValue(request, FlattenObjectResponse.class);
  }

  @Path("testFluentSetterFlattenObjectParam")
  @POST
  @Override
  public FluentSetterFlattenObjectResponse testFluentSetterFlattenObjectParam(
      FluentSetterFlattenObjectRequest request) {
    return RestObjectMapperFactory.getRestObjectMapper().convertValue(request, FluentSetterFlattenObjectResponse.class);
  }

  @Path("testMultiLayerObjectParam")
  @PUT
  @Override
  public MultiLayerObjectParam testMultiLayerObjectParam(MultiLayerObjectParam request) {
    return request;
  }

  @Path("testRecursiveObjectParam")
  @POST
  @Override
  public RecursiveObjectParam testRecursiveObjectParam(RecursiveObjectParam request) {
    return request;
  }

  @Path("testListObjectParam")
  @POST
  @Override
  public List<GenericObjectParam<List<RecursiveObjectParam>>> testListObjectParam(
      List<GenericObjectParam<List<RecursiveObjectParam>>> request) {
    return request;
  }

  @Path("testMapObjectParam")
  @POST
  @Override
  public Map<String, GenericObjectParam<Map<String, GenericObjectParam<RecursiveObjectParam>>>> testMapObjectParam(
      Map<String, GenericObjectParam<Map<String, GenericObjectParam<RecursiveObjectParam>>>> request) {
    return request;
  }

  @Path("/beanParamRequest/{path}")
  @GET
  public BeanParamRequest testBeanParamRequest(@BeanParam BeanParamRequest request) {
    return request;
  }

  @Path("/fluentSetterBeanParamRequest/{path}")
  @GET
  public FluentSetterBeanParamRequest testFluentSetterBeanParamRequest(
      @BeanParam FluentSetterBeanParamRequest request) {
    return request;
  }
}


