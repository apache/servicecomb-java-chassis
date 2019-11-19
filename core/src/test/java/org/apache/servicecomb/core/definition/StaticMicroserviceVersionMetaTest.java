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

package org.apache.servicecomb.core.definition;

public class StaticMicroserviceVersionMetaTest {
//
//  private static final String APP_ID_FROM_REGISTRY_UTIL = "appIdFromRegistryUtil";
//
//  @BeforeClass
//  public static void beforeClass() {
//    new MockUp<RegistryUtils>() {
//      @Mock
//      String getAppId() {
//        return APP_ID_FROM_REGISTRY_UTIL;
//      }
//    };
//  }
//
//  @Test
//  public void testConstruct() {
//    StaticMicroservice staticMicroservice = new StaticMicroservice();
//    String appId = "testAppId";
//    String serviceName = "testServiceName";
//    String version = "1.2.1";
//    staticMicroservice.setAppId(appId);
//    staticMicroservice.setServiceName(serviceName);
//    staticMicroservice.setVersion(version);
//
//    Holder<Boolean> schemaLoaded = new Holder<>(false);
//    SCBEngine.getInstance().setStaticSchemaFactory(new MockUp<StaticSchemaFactory>() {
//      @Mock
//      void loadSchema(MicroserviceMeta microserviceMeta, StaticMicroservice microservice) {
//        Assert.assertSame(APP_ID_FROM_REGISTRY_UTIL, microserviceMeta.getAppId());
//        Assert.assertSame(serviceName, microserviceMeta.getName());
//        Assert.assertSame(serviceName, microserviceMeta.getShortName());
//        Assert.assertSame(staticMicroservice, microservice);
//        schemaLoaded.value = true;
//      }
//    }.getMockInstance());
//    Holder<Boolean> listenerNotified = new Holder<>(false);
//    CseContext.getInstance().setSchemaListenerManager(new MockUp<SchemaListenerManager>() {
//      @Mock
//      void notifySchemaListener(MicroserviceMeta... microserviceMetas) {
//        Assert.assertEquals(1, microserviceMetas.length);
//        MicroserviceMeta microserviceMeta = microserviceMetas[0];
//        Assert.assertEquals(serviceName, microserviceMeta.getShortName());
//        listenerNotified.value = true;
//      }
//    }.getMockInstance());
//
//    StaticMicroserviceVersionMeta staticMicroserviceVersionMeta = new StaticMicroserviceVersionMeta(staticMicroservice);
//
//    Assert.assertTrue(schemaLoaded.value);
//    Assert.assertTrue(listenerNotified.value);
//    Assert.assertSame(staticMicroservice, staticMicroserviceVersionMeta.getMicroservice());
//    Assert.assertEquals(serviceName, staticMicroserviceVersionMeta.getMicroserviceMeta().getName());
//  }
}