/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.demo.jaxrs.tests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.OK;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class JaxrsIntegrationTest {

  private final RestTemplate restTemplate = new RestTemplate();

  @BeforeClass
  public static void setUp() throws Exception {
    JaxrsTestMain.main(new String[0]);
  }

  @Test
  public void ableToQueryAtRootBasePath() {
    ResponseEntity<String> responseEntity = restTemplate
        .getForEntity("http://127.0.0.1:8080/sayHi?name=Mike", String.class);

    assertThat(responseEntity.getStatusCode(), is(OK));
    assertThat(responseEntity.getBody(), is("Hi Mike"));
  }

  @Test
  public void ableToQueryAtRootPath() {
    ResponseEntity<String> responseEntity = restTemplate
        .getForEntity("http://127.0.0.1:8080/", String.class);

    assertThat(responseEntity.getStatusCode(), is(OK));
    assertThat(responseEntity.getBody(), is("Welcome home"));
  }

  @Test
  public void ableToQueryAtNonRootPath() {
    ResponseEntity<String> responseEntity = restTemplate
        .getForEntity("http://127.0.0.1:8080/french/bonjour?name=Mike", String.class);

    assertThat(responseEntity.getStatusCode(), is(OK));
    assertThat(responseEntity.getBody(), is("Bonjour Mike"));
  }
}
