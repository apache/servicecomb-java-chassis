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

package org.apache.servicecomb.registry.lightweight;

import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.servicecomb.config.YAMLUtil;
import org.apache.servicecomb.core.Endpoint;
import org.mockito.Mockito;

public class TestBase {
  static Endpoint endpoint = Mockito.mock(Endpoint.class);

  static class MockRegisterRequest extends RegisterRequest {
    @Override
    public Endpoint selectFirstEndpoint() {
      return endpoint;
    }
  }

  static AbstractConfiguration configuration = build();

  static AbstractConfiguration build() {
    Map<String, Object> map = YAMLUtil.yaml2Properties(""
        + "servicecomb:\n"
        + "  service:\n"
        + "    application: app\n"
        + "    name: svc\n"
        + "    version: 1.0.0.0");
    return new MapConfiguration(map);
  }

  Self self = new Self() {
    @Override
    protected RegisterRequest createRegisterRequest() {
      return new MockRegisterRequest();
    }
  }
      .init(configuration)
      .addSchema("schema-1", "s1")
      .addEndpoint("rest://1.1.1.1:80");
}
