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

import java.util.Arrays;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.provider.rest.common.RestSchema;

import io.swagger.annotations.ApiParam;

@RestSchema(schemaId = "dataTypeJaxrs")
@Path("/v1/dataTypeJaxrs")
public class DataTypeJaxrsSchema {
  @Path("intPath/{input}")
  @GET
  public int intPath(@PathParam("input") int input) {
    return input;
  }

  @Path("intQuery")
  @GET
  public int intQuery(@QueryParam("input") int input) {
    return input;
  }

  @Path("intHeader")
  @GET
  public int intHeader(@HeaderParam("input") int input) {
    return input;
  }

  @Path("intCookie")
  @GET
  public int intCookie(@CookieParam("input") int input) {
    return input;
  }

  @Path("intForm")
  @POST
  public int intForm(@FormParam("input") int input) {
    return input;
  }

  @Path("intBody")
  @POST
  public int intBody(int input) {
    return input;
  }

  @Path("intAdd")
  @GET
  public int intAdd(@QueryParam("num1") int num1, @QueryParam("num2") int num2) {
    return num1 + num2;
  }

  //string
  @Path("stringPath/{input}")
  @GET
  public String stringPath(@PathParam("input") String input) {
    return input;
  }

  @Path("stringQuery")
  @GET
  public String stringQuery(@QueryParam("input") String input) {
    return input;
  }

  @Path("stringHeader")
  @GET
  public String stringHeader(@HeaderParam("input") String input) {
    return input;
  }

  @Path("stringCookie")
  @GET
  public String stringCookie(@CookieParam("input") String input) {
    return input;
  }

  @Path("stringForm")
  @POST
  public String stringForm(@FormParam("input") String input) {
    return input;
  }

  @Path("stringBody")
  @POST
  public String stringBody(String input) {
    return input;
  }

  @Path("stringConcat")
  @GET
  public String stringConcat(@QueryParam("str1") String str1, @QueryParam("str2") String str2) {
    return str1 + str2;
  }

  //double
  @Path("doublePath/{input}")
  @GET
  public double doublePath(@PathParam("input") double input) {
    return input;
  }

  @Path("doubleQuery")
  @GET
  public double doubleQuery(@QueryParam("input") double input) {
    return input;
  }

  @Path("doubleHeader")
  @GET
  public double doubleHeader(@HeaderParam("input") double input) {
    return input;
  }

  @Path("doubleCookie")
  @GET
  public double doubleCookie(@CookieParam("input") double input) {
    return input;
  }

  @Path("doubleForm")
  @POST
  public double doubleForm(@FormParam("input") double input) {
    return input;
  }

  @Path("doubleBody")
  @POST
  public double doubleBody(double input) {
    return input;
  }

  @Path("doubleAdd")
  @GET
  public double doubleAdd(@QueryParam("num1") double num1, @QueryParam("num2") double num2) {
    return num1 + num2;
  }

  // float
  @Path("floatPath/{input}")
  @GET
  public float floatPath(@PathParam("input") float input) {
    return input;
  }

  @Path("floatQuery")
  @GET
  public float floatQuery(@QueryParam("input") float input) {
    return input;
  }

  @Path("floatHeader")
  @GET
  public float floatHeader(@HeaderParam("input") float input) {
    return input;
  }

  @Path("floatCookie")
  @GET
  public float floatCookie(@CookieParam("input") float input) {
    return input;
  }

  @Path("floatForm")
  @POST
  public float floatForm(@FormParam("input") float input) {
    return input;
  }

  @Path("floatBody")
  @POST
  public float floatBody(float input) {
    return input;
  }

  @Path("floatAdd")
  @GET
  public float floatAdd(@QueryParam("num1") float num1, @QueryParam("num2") float num2) {
    return num1 + num2;
  }

  @Path("enumBody")
  @POST
  public Color enumBody(Color color) {
    return color;
  }

  // query array
  @Path("queryArr")
  @GET
  public String queryArr(@QueryParam("queryArr") String[] queryArr) {
    return Arrays.toString(queryArr) + queryArr.length;
  }

  @Path("queryArrCSV")
  @GET
  public String queryArrCSV(@ApiParam(collectionFormat = "csv") @QueryParam("queryArr") String[] queryArr) {
    return Arrays.toString(queryArr) + queryArr.length;
  }

  @Path("queryArrSSV")
  @GET
  public String queryArrSSV(@ApiParam(collectionFormat = "ssv") @QueryParam("queryArr") String[] queryArr) {
    return Arrays.toString(queryArr) + queryArr.length;
  }

  @Path("queryArrTSV")
  @GET
  public String queryArrTSV(@ApiParam(collectionFormat = "tsv") @QueryParam("queryArr") String[] queryArr) {
    return Arrays.toString(queryArr) + queryArr.length;
  }

  @Path("queryArrPIPES")
  @GET
  public String queryArrPIPES(@ApiParam(collectionFormat = "pipes") @QueryParam("queryArr") String[] queryArr) {
    return Arrays.toString(queryArr) + queryArr.length;
  }

  @Path("queryArrMULTI")
  @GET
  public String queryArrMULTI(@ApiParam(collectionFormat = "multi") @QueryParam("queryArr") String[] queryArr) {
    return Arrays.toString(queryArr) + queryArr.length;
  }
}
