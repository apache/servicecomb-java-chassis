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

import com.fasterxml.jackson.databind.JavaType;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.vertx.http.ReadStreamPart;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.Part;
import java.io.File;
import java.lang.reflect.Method;

public class TestInvoker {
  public interface DownloadIntf {
    ReadStreamPart download();
  }

  public static class DownloadSchema {
    public File download() {
      return null;
    }
  }

  @BeforeEach
  public void setUp() {
    ConfigUtil.installDynamicConfig();
    DiscoveryManager.renewInstance();
  }

  @AfterEach
  public void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void should_generate_response_meta_for_download() throws NoSuchMethodException {
    SCBEngine scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("download", new DownloadSchema()).run();

    PojoConsumerMetaRefresher refresher = new PojoConsumerMetaRefresher(
        scbEngine.getProducerMicroserviceMeta().getMicroserviceName(),
        "download",
        DownloadIntf.class);

    Method method = DownloadIntf.class.getMethod("download");
    JavaType javaType = refresher.getLatestMeta().ensureFindOperationMeta(method).getResponsesType();
    Assertions.assertSame(Part.class, javaType.getRawClass());

    scbEngine.destroy();
  }
}
