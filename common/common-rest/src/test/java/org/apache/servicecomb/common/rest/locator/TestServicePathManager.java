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

package org.apache.servicecomb.common.rest.locator;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.common.utils.ClassLoaderScopeContext;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestServicePathManager {
  @Before
  public void setUp() {
    ConfigUtil.installDynamicConfig();
    RegistryUtils.initWithLocalRegistry();
  }
  @After
  public void tearDown() {
    ArchaiusUtils.resetConfig();
    ClassLoaderScopeContext.clearClassLoaderScopeProperty();
  }

  @Test
  public void testBuildProducerPathsNoPrefix() {
    SCBEngine scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("sid1", new TestPathSchema())
        .run();
    ServicePathManager spm = ServicePathManager.getServicePathManager(scbEngine.getProducerMicroserviceMeta());

    Assert.assertSame(spm.producerPaths, spm.swaggerPaths);

    scbEngine.destroy();
  }

  @Test
  public void testBuildProducerPathsHasPrefix() {
    ClassLoaderScopeContext.setClassLoaderScopeProperty(DefinitionConst.URL_PREFIX, "/root/rest");

    SCBEngine scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("sid1", new TestPathSchema())
        .run();
    ServicePathManager spm = ServicePathManager.getServicePathManager(scbEngine.getProducerMicroserviceMeta());

    // all locate should be success
    spm.producerLocateOperation("/root/rest/static/", "GET");
    spm.producerLocateOperation("/root/rest/static/", "POST");
    spm.producerLocateOperation("/root/rest/dynamic/1/", "GET");
    spm.producerLocateOperation("/root/rest/dynamicEx/1/", "GET");

    scbEngine.destroy();
  }
}
