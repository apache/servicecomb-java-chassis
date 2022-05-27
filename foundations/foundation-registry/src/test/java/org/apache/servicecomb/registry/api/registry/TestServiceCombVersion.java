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

package org.apache.servicecomb.registry.api.registry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestServiceCombVersion {
  @Test
  public void testServiceCombVersion() {
    ServiceCombVersion version = new ServiceCombVersion();
    // this.getClass().getPackage().getImplementationVersion()
    // is not stable, may get null or 2.0.1-SNAPSHOT
    Assertions.assertTrue(version.loadVersion().toString().contains("ServiceComb"));
  }
}
