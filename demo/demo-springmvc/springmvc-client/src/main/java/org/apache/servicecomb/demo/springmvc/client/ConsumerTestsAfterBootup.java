package org.apache.servicecomb.demo.springmvc.client;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.registry.RegistrationManager;
import org.springframework.stereotype.Component;

/**
 * Testing after bootup.
 */
@Component
public class ConsumerTestsAfterBootup implements BootListener {
  private void testRegisterPath() {
    TestMgr.check(RegistrationManager.INSTANCE.getMicroservice().getPaths().size(), 0);
  }

  private void testSchemaContent() {
    String content = RegistrationManager.INSTANCE.getMicroservice().getSchemaMap().get("SpringMVCSchema");
    TestMgr.check(content.replaceAll("\\s", ""),
        readFile("SpringMVCSchema.yaml").replaceAll("[\\s#]", ""));
  }

  private String readFile(String restController) {
    // test code, make simple
    try {
      InputStream inputStream = this.getClass().getResource("/" + restController).openStream();
      byte[] buffer = new byte[2048 * 10];
      inputStream.skip(1000);
      int len = inputStream.read(buffer);
      TestMgr.check(2048 * 10 > len, true);
      inputStream.close();
      return new String(buffer, 0, len, Charset.forName("UTF-8"));
    } catch (IOException e) {
      TestMgr.failed(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public void onBootEvent(BootEvent event) {
    if (event.getEventType() == BootListener.EventType.AFTER_REGISTRY) {
      testRegisterPath();
      testSchemaContent();
      if (!TestMgr.isSuccess()) {
        TestMgr.summary();
        throw new IllegalStateException("some tests are failed. ");
      }
    }
  }
}
