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

package org.apache.servicecomb.provider.pojo;

import java.io.File;

import javax.servlet.http.Part;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.vertx.http.ReadStreamPart;
import org.apache.servicecomb.provider.pojo.definition.PojoConsumerMeta;
import org.apache.servicecomb.serviceregistry.DiscoveryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;

import mockit.Deencapsulation;

public class TestInvoker {
  public interface DownloadIntf {
    ReadStreamPart download();
  }

  public class DownloadSchema {
    public File download() {
      return null;
    }
  }

  @Before
  public void setUp() {
    ConfigUtil.installDynamicConfig();
    DiscoveryManager.renewInstance();
  }

  @After
  public void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void should_generate_response_meta_for_download() {
    SCBEngine scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("download", new DownloadSchema()).run();
    Invoker invoker = new Invoker(scbEngine.getProducerMicroserviceMeta().getMicroserviceName(), "download",
        DownloadIntf.class);
    Deencapsulation.invoke(invoker, "ensureStatusUp");
    PojoConsumerMeta meta = Deencapsulation.invoke(invoker, "refreshMeta");

    JavaType javaType = meta.findOperationMeta("download").getResponsesType();
    Assert.assertSame(Part.class, javaType.getRawClass());

    scbEngine.destroy();
  }
}
