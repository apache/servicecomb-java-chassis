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

package org.apache.servicecomb.provider.springmvc.reference;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import java.net.URI;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import com.seanyinx.github.unit.scaffolding.Randomness;

public class TestRestTemplateBuilder {

  private final String url = Randomness.uniquify("url");

  private final AcceptableRestTemplate underlying = new AlwaysAcceptableRestTemplate();

  private static class AlwaysAcceptableRestTemplate extends AcceptableRestTemplate {

    @Override
    public boolean isAcceptable(String uri) {
      return true;
    }

    @Override
    public boolean isAcceptable(URI uri) {
      return true;
    }
  }

  @Test
  public void addsRestTemplateToWrapper() {
    RestTemplateBuilder.addAcceptableRestTemplate(1, underlying);

    RestTemplate restTemplate = RestTemplateBuilder.create();

    assertThat(restTemplate, instanceOf(RestTemplateWrapper.class));

    RestTemplateWrapper wrapper = (RestTemplateWrapper) restTemplate;

    assertThat(wrapper.getRestTemplate(url), is(underlying));
    assertThat(wrapper.getRestTemplate(URI.create(url)), is(underlying));
  }
}
