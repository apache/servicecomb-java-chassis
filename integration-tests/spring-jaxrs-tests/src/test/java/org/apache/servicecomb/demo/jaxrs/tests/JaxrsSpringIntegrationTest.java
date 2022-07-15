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

package org.apache.servicecomb.demo.jaxrs.tests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.netflix.config.DynamicProperty;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = JaxrsSpringMain.class)
public class JaxrsSpringIntegrationTest extends JaxrsIntegrationTestBase {
  @BeforeAll
  public static void setUp() {
    System.setProperty("property.test5", "from_system_property");
  }

  @AfterAll
  public static void tearDown() {
    System.clearProperty("property.test5");
  }

  @Test
  public void testGetConfigFromSpringBoot() {
    DynamicProperty dynamicProperty = DynamicProperty.getInstance("property.test0");
    Assertions.assertEquals("from_properties", dynamicProperty.getString());
    dynamicProperty = DynamicProperty.getInstance("property.test1");
    Assertions.assertEquals("from_yml", dynamicProperty.getString());
    dynamicProperty = DynamicProperty.getInstance("property.test2");
    Assertions.assertEquals("from_yaml_from_yml", dynamicProperty.getString());
    dynamicProperty = DynamicProperty.getInstance("property.test3");
    Assertions.assertEquals("from_yaml_dev_from_properties", dynamicProperty.getString());
    dynamicProperty = DynamicProperty.getInstance("property.test4");
    Assertions.assertEquals("from_microservice_yaml", dynamicProperty.getString());
    dynamicProperty = DynamicProperty.getInstance("property.test5");
    Assertions.assertEquals("from_system_property", dynamicProperty.getString());
  }
}
