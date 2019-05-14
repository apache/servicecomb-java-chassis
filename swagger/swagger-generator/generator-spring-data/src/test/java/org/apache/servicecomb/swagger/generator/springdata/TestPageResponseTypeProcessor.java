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
package org.apache.servicecomb.swagger.generator.springdata;

import java.io.IOException;

import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.Page;

import io.swagger.util.Json;

public class TestPageResponseTypeProcessor {
  interface PageSchema {
    Page<String> test(Page<String> page);
  }

  @Test
  public void swagger() {
    UnitTestSwaggerUtils.testSwagger("pageSchema.yaml", PageSchema.class);
  }

  @Test
  public void deserialize() throws IOException {
    Json.mapper().registerModule(new SpringDataModule());

    String json = "{\"content\":[\"c1\",\"c2\"],\"pageable\":{\"page\":1,\"size\":2}}";
    Page<?> page = Json.mapper().readValue(json, Page.class);

    Assert.assertEquals(
        "{\"content\":[\"c1\",\"c2\"],\"pageable\":{\"offset\":2,\"pageNumber\":1,\"pageSize\":2,\"paged\":true,\"sort\":{\"sorted\":false,\"unsorted\":true},\"unpaged\":false},\"first\":false,\"last\":true,\"number\":1,\"numberOfElements\":2,\"size\":2,\"sort\":{\"sorted\":false,\"unsorted\":true},\"totalElements\":4,\"totalPages\":2}",
        Json.mapper().writeValueAsString(page));
  }
}
