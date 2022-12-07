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

package org.apache.servicecomb.governance;

import org.apache.servicecomb.governance.handler.MapperHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.processor.mapping.Mapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GovernanceConfiguration.class, MockConfiguration.class})
public class MapperTest {
  private MapperHandler mapperHandler;

  private MapperHandler mapperHandler2;

  @Autowired
  public void setMapperHandler(MapperHandler mapperHandler, @Qualifier("mapperHandler2") MapperHandler mapperHandler2) {
    this.mapperHandler = mapperHandler;
    this.mapperHandler2 = mapperHandler2;
  }

  @Test
  public void test_mapper_work() {
    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/mapper/v1");
    Mapper mapper = mapperHandler.getActuator(request);
    Assertions.assertEquals(2, mapper.target().size());
    Assertions.assertEquals("127.0.0.1", mapper.target().get("host"));
    Assertions.assertEquals("8080", mapper.target().get("port"));
  }

  @Test
  public void test_mapper2_work() {
    GovernanceRequest request = new GovernanceRequest();
    request.setUri("/mapper/v1");
    Mapper mapper = mapperHandler2.getActuator(request);
    Assertions.assertEquals(2, mapper.target().size());
    Assertions.assertEquals("127.0.0.1", mapper.target().get("host"));
    Assertions.assertEquals("9090", mapper.target().get("port"));
  }
}
