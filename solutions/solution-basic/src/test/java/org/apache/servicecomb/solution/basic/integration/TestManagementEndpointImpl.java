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
package org.apache.servicecomb.solution.basic.integration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestManagementEndpointImpl {
  @Test
  public void testSchemaContentsParameterNotValid() {
    ManagementEndpoint endpoint = new ManagementEndpointImpl();
    Assertions.assertThrows(InvocationException.class, () -> endpoint.schemaContents(null));
    Assertions.assertThrows(InvocationException.class, () -> endpoint.schemaContents(Collections.emptySet()));
    final Set<String> ids = new HashSet<>();
    ids.add("33/33");
    Assertions.assertThrows(InvocationException.class, () -> endpoint.schemaContents(ids));
    final Set<String> ids2 = new HashSet<>();
    ids2.add("eeee\\");
    Assertions.assertThrows(InvocationException.class, () -> endpoint.schemaContents(ids2));
    final Set<String> ids3 = new HashSet<>();
    ids3.add("eeee3333..sss");
    Assertions.assertThrows(InvocationException.class, () -> endpoint.schemaContents(ids3));
    final Set<String> ids4 = new HashSet<>();
    ids4.add("eeee3333/sss4");
    Assertions.assertThrows(InvocationException.class, () -> endpoint.schemaContents(ids4));
  }
}
