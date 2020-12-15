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
import java.util.Arrays;

import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

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

    Sort sort = Sort.by(Direction.ASC, "name");
    Pageable pageable = PageRequest.of(1, 10, sort);
    Page<String> page = new PageImpl<>(Arrays.asList("c1", "c2"), pageable, 2);
    String json = Json.mapper().writeValueAsString(page);
    Assert.assertEquals(
        "{\"content\":[\"c1\",\"c2\"],\"pageable\":{\"pageNumber\":1,\"pageSize\":10,\"sort\":{\"properties\":[\"name\"]},\"offset\":10,\"paged\":true,\"unpaged\":false},\"empty\":false,\"first\":false,\"last\":true,\"number\":1,\"numberOfElements\":2,\"size\":10,\"sort\":{\"properties\":[\"name\"]},\"totalElements\":12,\"totalPages\":2}",
        json);

    Page<?> page2 = Json.mapper().readValue(json, Page.class);

    Assert.assertEquals(json,
        Json.mapper().writeValueAsString(page2));
  }
}
