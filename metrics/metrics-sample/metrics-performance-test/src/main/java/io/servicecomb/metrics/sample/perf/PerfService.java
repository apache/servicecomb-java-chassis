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

package io.servicecomb.metrics.sample.perf;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.servicecomb.metrics.core.publish.DataSource;
import io.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "demoServiceEndpoint")
@RequestMapping(path = "/")
public class PerfService {

  private final DataSource dataSource;

  @Autowired
  public PerfService(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @GetMapping(path = "/f0")
  public String fun0() {
    return UUID.randomUUID().toString();
  }

  @GetMapping(path = "/f1")
  public String fun1() {
    return UUID.randomUUID().toString();
  }

  @GetMapping(path = "/f2")
  public String fun2() {
    return UUID.randomUUID().toString();
  }

  @GetMapping(path = "/f3")
  public String fun3() {
    return UUID.randomUUID().toString();
  }

  @GetMapping(path = "/f4")
  public String fun4() {
    return UUID.randomUUID().toString();
  }

  @GetMapping(path = "/f5")
  public String fun5() {
    return UUID.randomUUID().toString();
  }

  @GetMapping(path = "/f6")
  public String fun6() {
    return UUID.randomUUID().toString();
  }

  @GetMapping(path = "/f7")
  public String fun7() {
    return UUID.randomUUID().toString();
  }

  @GetMapping(path = "/f8")
  public String fun8() {
    return UUID.randomUUID().toString();
  }

  @GetMapping(path = "/f9")
  public String fun9() {
    return UUID.randomUUID().toString();
  }
}
