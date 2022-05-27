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

package org.apache.servicecomb.demo.jaxrs.client;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TestQueryParamWithListSchema implements CategorizedTestCase {
  private RestTemplate restTemplate = RestTemplateBuilder.create();

  @Override
  public void testAllTransport() throws Exception {
    testMulti();
    testCSV();
    testSSV();
    testTSV();
    testPipes();
  }

  @Override
  public void testRestTransport() throws Exception {
    testMultiRest();
    testCSVRest();
    testSSVRest();
    testTSVRest();
    testPipesRest();
  }

  @Override
  // highway do not handle empty/default/null
  public void testHighwayTransport() throws Exception {
    testMultiHighway();
    testCSVHighway();
    testSSVHighway();
    testTSVHighway();
    testPipesHighway();
  }

  private void testCSVHighway() {
    TestMgr.check("null",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListCSV?", String.class));
  }

  private void testCSVRest() {
    TestMgr.check("0:[]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListCSV?", String.class));
  }

  private void testSSV() {
    TestMgr.check("2:[1, 2]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListSSV?queryList=1%202", String.class));
    TestMgr.check("2:[, ]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListSSV?queryList=%20", String.class));
    TestMgr.check("1:[]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListSSV?queryList=", String.class));
  }

  private void testTSVHighway() {
    TestMgr.check("null",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListTSV?", String.class));
  }

  private void testTSVRest() {
    TestMgr.check("0:[]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListTSV?", String.class));
  }

  private void testTSV() {
    TestMgr.check("2:[1, 2]",
        restTemplate
            .getForObject("cse://jaxrs/queryList/queryListTSV?queryList={1}", String.class, "1\t2"));
    TestMgr.check("2:[, ]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListTSV?queryList={1}", String.class, "\t"));
    TestMgr.check("1:[]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListTSV?queryList=", String.class));
  }

  private void testPipesHighway() {
    TestMgr.check("null",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListPIPES?", String.class));
  }

  private void testPipesRest() {
    TestMgr.check("0:[]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListPIPES?", String.class));
  }

  private void testPipes() {
    TestMgr.check("2:[1, 2]",
        restTemplate
            .getForObject("cse://jaxrs/queryList/queryListPIPES?queryList={1}", String.class, "1|2"));
    TestMgr.check("2:[, ]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListPIPES?queryList={1}", String.class, "|"));
    TestMgr.check("1:[]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListPIPES?queryList=", String.class));
  }

  private void testSSVHighway() {
    TestMgr.check("null",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListSSV?", String.class));
  }

  private void testSSVRest() {
    TestMgr.check("0:[]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListSSV?", String.class));
  }

  private void testCSV() {
    TestMgr.check("2:[1, 2]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListCSV?queryList=1,2", String.class));
    TestMgr.check("2:[, ]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListCSV?queryList=,", String.class));
    TestMgr.check("1:[]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListCSV?queryList=", String.class));
  }

  private void testMultiHighway() {
    TestMgr.check("null",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListMULTI?", String.class));
  }

  private void testMultiRest() {
    TestMgr.check("0:[]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListMULTI?", String.class));
  }

  private void testMulti() {
    TestMgr.check("2:[1, 2]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListMULTI?queryList=1&queryList=2", String.class));
    TestMgr.check("2:[, ]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListMULTI?queryList=&queryList=", String.class));
    TestMgr.check("1:[]",
        restTemplate.getForObject("cse://jaxrs/queryList/queryListMULTI?queryList=", String.class));
  }
}
