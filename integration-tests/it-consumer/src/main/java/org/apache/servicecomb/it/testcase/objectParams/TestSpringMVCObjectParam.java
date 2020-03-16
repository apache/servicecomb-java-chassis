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
package org.apache.servicecomb.it.testcase.objectParams;

import static org.junit.Assert.assertEquals;

import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.schema.objectParams.QueryObjectModel;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class TestSpringMVCObjectParam {
  interface SpringMVCObjectIntf {
    String testQueryObjectParam(int index, String name);

    String testQueryObjectWithHeader(String prefix, int index, String name);

    String testQueryObjectWithHeaderName(String prefix, int index, String name);

    String testQueryObjectWithHeaderValue(String prefix, int index, String name);

    String testQueryObjectWithHeaderValueAndName(String prefix, String suffix, int index, String name);

    String testQueryObjectWithParam(String prefix, int index, String name);

    String testQueryObjectWithParamName(String prefix, int index, String name);

    String testQueryObjectWithParamValue(String prefix, int index, String name);
  }

  private String prefix = "prefix-";

  private String suffix = "-suffix";

  private QueryObjectModel queryModel = new QueryObjectModel(23, "demo");

  private String queryParam = "index=23&name=demo";

  private static Consumers<SpringMVCObjectIntf> consumersSpringmvc = new Consumers<>("springMVCObjectParamSchema",
      SpringMVCObjectIntf.class);

  @Test
  public void testQueryObjectParam_rt() {
    assertEquals(queryModel.toString(), consumersSpringmvc.getSCBRestTemplate()
        .getForObject("/testQueryObjectParam?" + queryParam, String.class));
  }

  @Test
  public void testQueryObjectParam_pojo() {
    assertEquals(queryModel.toString(), consumersSpringmvc.getIntf().testQueryObjectParam(23, "demo"));
  }

  @Test
  public void testQueryObjectWithHeader_rt() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("prefix", prefix);
    assertEquals(prefix + queryModel.toString(),
        queryObjectHeader(consumersSpringmvc, headers, "/testQueryObjectWithHeader?" + queryParam));
  }

  @Test
  public void testQueryObjectWithHeader_pojo() {
    assertEquals(prefix + queryModel.toString(),
        consumersSpringmvc.getIntf().testQueryObjectWithHeader(prefix, 23, "demo"));
  }

  @Test
  public void testQueryObjectWithHeaderName_rt() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("prefix", prefix);
    assertEquals(prefix + queryModel.toString(),
        queryObjectHeader(consumersSpringmvc, headers, "/testQueryObjectWithHeaderName?" + queryParam));
  }

  @Test
  public void testQueryObjectWithHeaderName_pojo() {
    assertEquals(prefix + queryModel.toString(),
        consumersSpringmvc.getIntf().testQueryObjectWithHeaderName(prefix, 23, "demo"));
  }

  @Test
  public void testQueryObjectWithHeaderValue_rt() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("prefix", prefix);
    assertEquals(prefix + queryModel.toString(),
        queryObjectHeader(consumersSpringmvc, headers, "/testQueryObjectWithHeaderValue?" + queryParam));
  }

  @Test
  public void testQueryObjectWithHeaderValue_pojo() {
    assertEquals(prefix + queryModel.toString(),
        consumersSpringmvc.getIntf().testQueryObjectWithHeaderValue(prefix, 23, "demo"));
  }

  @Test
  public void testQueryObjectWithHeaderValueAndName_rt() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("prefix", prefix);
    headers.add("suffix", suffix);
    assertEquals(prefix + queryModel.toString() + suffix,
        queryObjectHeader(consumersSpringmvc, headers, "/testQueryObjectWithHeaderValueAndName?" + queryParam));
  }

  @Test
  public void testQueryObjectWithHeaderValueAndName_pojo() {
    assertEquals(prefix + queryModel.toString() + suffix,
        consumersSpringmvc.getIntf().testQueryObjectWithHeaderValueAndName(prefix, suffix, 23, "demo"));
  }

  @Test
  public void testQueryObjectWithParam_rt() {
    assertEquals(prefix + queryModel.toString(), consumersSpringmvc.getSCBRestTemplate()
        .getForObject("/testQueryObjectWithParam?prefix=" + prefix + "&" + queryParam, String.class));
  }

  @Test
  public void testQueryObjectWithParam_pojo() {
    assertEquals(prefix + queryModel.toString(),
        consumersSpringmvc.getIntf().testQueryObjectWithParam(prefix, 23, "demo"));
  }

  @Test
  public void testQueryObjectWithParamName_rt() {
    assertEquals(prefix + queryModel.toString(), consumersSpringmvc.getSCBRestTemplate()
        .getForObject("/testQueryObjectWithParamName?prefix=" + prefix + "&" + queryParam, String.class));
  }

  @Test
  public void testQueryObjectWithParamName_pojo() {
    assertEquals(prefix + queryModel.toString(),
        consumersSpringmvc.getIntf().testQueryObjectWithParamName(prefix, 23, "demo"));
  }

  @Test
  public void testQueryObjectWithParamValue_rt() {
    assertEquals(prefix + queryModel.toString(), consumersSpringmvc.getSCBRestTemplate()
        .getForObject("/testQueryObjectWithParamValue?prefix=" + prefix + "&" + queryParam, String.class));
  }

  @Test
  public void testQueryObjectWithParamValue_pojo() {
    assertEquals(prefix + queryModel.toString(),
        consumersSpringmvc.getIntf().testQueryObjectWithParamValue(prefix, 23, "demo"));
  }

  protected String queryObjectHeader(Consumers<SpringMVCObjectIntf> consumers, HttpHeaders headers, String url) {
    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = consumers.getSCBRestTemplate()
        .exchange(url,
            HttpMethod.GET,
            entity,
            String.class);
    return response.getBody();
  }
}
