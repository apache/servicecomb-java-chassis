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
package org.apache.servicecomb.it;

import org.apache.servicecomb.common.rest.HttpTransportContext;
import org.apache.servicecomb.common.rest.VertxHttpTransportContext;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.it.deploy.Deploys;
import org.apache.servicecomb.it.deploy.MicroserviceDeploy;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.apache.servicecomb.it.schema.TestApiOperation;
import org.apache.servicecomb.it.schema.generic.TestMyService;
import org.apache.servicecomb.it.testcase.TestAcceptType;
import org.apache.servicecomb.it.testcase.TestAnnotatedAttribute;
import org.apache.servicecomb.it.testcase.TestApiOperationOverride;
import org.apache.servicecomb.it.testcase.TestApiParam;
import org.apache.servicecomb.it.testcase.TestAsyncInvoke;
import org.apache.servicecomb.it.testcase.TestChangeTransport;
import org.apache.servicecomb.it.testcase.TestDataTypePrimitive;
import org.apache.servicecomb.it.testcase.TestDefaultJsonValueJaxrsSchema;
import org.apache.servicecomb.it.testcase.TestDefaultValue;
import org.apache.servicecomb.it.testcase.TestDownload;
import org.apache.servicecomb.it.testcase.TestDownloadSlowStreamEdge;
import org.apache.servicecomb.it.testcase.TestExceptionConvertEdge;
import org.apache.servicecomb.it.testcase.TestGenericEdge;
import org.apache.servicecomb.it.testcase.TestIgnoreMethod;
import org.apache.servicecomb.it.testcase.TestIgnoreStaticMethod;
import org.apache.servicecomb.it.testcase.TestJsonView;
import org.apache.servicecomb.it.testcase.TestOptional;
import org.apache.servicecomb.it.testcase.TestParamCodec;
import org.apache.servicecomb.it.testcase.TestParamCodecEdge;
import org.apache.servicecomb.it.testcase.TestRequestBodySpringMvcSchema;
import org.apache.servicecomb.it.testcase.TestRestController;
import org.apache.servicecomb.it.testcase.TestRestServerConfigEdge;
import org.apache.servicecomb.it.testcase.TestRestVertxTransportConfig;
import org.apache.servicecomb.it.testcase.TestSpringConfiguration;
import org.apache.servicecomb.it.testcase.TestTrace;
import org.apache.servicecomb.it.testcase.TestTraceEdge;
import org.apache.servicecomb.it.testcase.TestTransportContext;
import org.apache.servicecomb.it.testcase.TestUpload;
import org.apache.servicecomb.it.testcase.base.TestGeneric;
import org.apache.servicecomb.it.testcase.objectparams.TestJAXRSObjectParamType;
import org.apache.servicecomb.it.testcase.objectparams.TestRPCObjectParamType;
import org.apache.servicecomb.it.testcase.objectparams.TestSpringMVCObjectParamType;
import org.apache.servicecomb.it.testcase.objectparams.TestSpringMVCObjectParamTypeRestOnly;
import org.apache.servicecomb.it.testcase.publicHeaders.TestPublicHeadersEdge;
import org.apache.servicecomb.it.testcase.thirdparty.Test3rdPartyInvocation;
import org.apache.servicecomb.it.testcase.weak.consumer.TestSpringmvcBasic;
import org.apache.servicecomb.transport.highway.HighwayTransportContext;

public class ConsumerMain {
  private static final ResultPrinter resultPrinter = new ResultPrinter();

  private static final Deploys deploys = new Deploys();

  public static boolean autoExit = true;

  public static void main(String[] args) throws Throwable {
    deploys.init();
    deploys.getServiceCenter().ensureReady();

    BeanUtils.init();
    ITUtils.waitBootFinished();

    try {
      run();
    } finally {
      SCBEngine.getInstance().destroy();
      deploys.getServiceCenter().stop();
    }

    resultPrinter.print();

    if (autoExit) {
      System.exit(0);
    }
  }

  protected static void run() throws Throwable {
    ITJUnitUtils.run(TestSpringConfiguration.class);

    // deploy edge/zuul
    // if not ready, will start a new instance and wait for ready
    deploys.getEdge().ensureReady();
    // deploys.getZuul().ensureReady(zuul);

    try {
      ITJUnitUtils.run(TestIgnoreStaticMethod.class);
      ITJUnitUtils.run(TestIgnoreMethod.class);
      ITJUnitUtils.run(TestApiParam.class);
      ITJUnitUtils.run(TestApiOperation.class);

      testOneProducer(deploys.getBaseProducer(), ConsumerMain::testStandalone);
      // Running H2, there are many dependencies, like JDk version, open ssl version
      // We can not guarantee the CI satisfy this. So do not running this test.
//      testOneProducer(deploys.getBaseHttp2Producer(), ConsumerMain::testH2Standalone);
      testOneProducer(deploys.getBaseHttp2CProducer(), ConsumerMain::testH2CStandalone);

      testOneProducer(deploys.getSpringBoot2StandaloneProducer(), ConsumerMain::testSpringBoot2Standalone);
      testOneProducer(deploys.getSpringBoot2ServletProducer(), ConsumerMain::testSpringBoot2Servlet);
    } finally {
      deploys.getEdge().stop();
    }
  }

  private static void runShareTestCases() throws Throwable {
    ITJUnitUtils.runWithHighwayAndRest(TestPublicHeadersEdge.class);
    ITJUnitUtils.runWithHighwayAndRest(TestChangeTransport.class);
    ITJUnitUtils.runWithHighwayAndRest(TestDataTypePrimitive.class);
    ITJUnitUtils.runWithHighwayAndRest(TestAnnotatedAttribute.class);
    ITJUnitUtils.runWithHighwayAndRest(TestMyService.class);

    //only rest support Json view
    ITJUnitUtils.runWithRest(TestJsonView.class);

    // only rest support default value feature
    ITJUnitUtils.runWithRest(TestDefaultValue.class);
    ITJUnitUtils.runWithRest(TestAcceptType.class);

    ITJUnitUtils.runWithRest(TestUpload.class);
    ITJUnitUtils.runWithRest(TestDownload.class);
    ITJUnitUtils.runWithHighwayAndRest(TestExceptionConvertEdge.class);

    ITJUnitUtils.runWithHighwayAndRest(TestTrace.class);
    ITJUnitUtils.run(TestTraceEdge.class);

    ITJUnitUtils.runWithHighwayAndRest(TestParamCodec.class);
    ITJUnitUtils.run(TestParamCodecEdge.class);

    //generic
    ITJUnitUtils.runWithRest(TestGeneric.class);
    ITJUnitUtils.run(TestGenericEdge.class);

    ITJUnitUtils.run(TestRequestBodySpringMvcSchema.class);
    ITJUnitUtils.run(TestDefaultJsonValueJaxrsSchema.class);
    ITJUnitUtils.run(TestRestController.class);
    ITJUnitUtils.runWithRest(TestRestController.class);

    ITJUnitUtils.runWithHighwayAndRest(TestAsyncInvoke.class);

    ITJUnitUtils.runWithHighwayAndRest(TestOptional.class);
    ITJUnitUtils.runWithHighwayAndRest(TestApiOperationOverride.class);

    ITJUnitUtils.runWithHighwayAndRest(TestSpringMVCObjectParamType.class);
    ITJUnitUtils.runWithHighwayAndRest(TestSpringMVCObjectParamTypeRestOnly.class);
    ITJUnitUtils.runWithHighwayAndRest(TestJAXRSObjectParamType.class);
    ITJUnitUtils.runWithHighwayAndRest(TestRPCObjectParamType.class);

    ITJUnitUtils.runWithHighwayAndRest(TestSpringmvcBasic.class);
  }

  interface ITTask {
    void run() throws Throwable;
  }

  private static void testOneProducer(MicroserviceDeploy microserviceDeploy, ITTask task) throws Throwable {
    microserviceDeploy.ensureReady();
    ITJUnitUtils.addProducer(microserviceDeploy.getMicroserviceDeployDefinition().getMicroserviceName());

    try {
      task.run();
    } finally {
      ITJUnitUtils.popProducer();
      microserviceDeploy.stop();
    }
  }

  private static void testStandalone() throws Throwable {
    runShareTestCases();

    // currently not support update 3rd url, so only test once
    ITJUnitUtils.run(Test3rdPartyInvocation.class);

    // about url len, different deploy have different url len, so only test standalone
    ITJUnitUtils.runWithRest(TestRestVertxTransportConfig.class);
    ITJUnitUtils.run(TestRestServerConfigEdge.class);

    // currently, only support vertx download
    ITJUnitUtils.run(TestDownloadSlowStreamEdge.class);

    TestTransportContext.expectName = VertxHttpTransportContext.class.getName();
    ITJUnitUtils.runWithRest(TestTransportContext.class);

    TestTransportContext.expectName = HighwayTransportContext.class.getName();
    ITJUnitUtils.runWithHighway(TestTransportContext.class);
  }

  private static void testH2CStandalone() throws Throwable {
    runShareTestCases();

    //as setMaxInitialLineLength() is not work for http2, do not need
    // ITJUnitUtils.runWithRest(TestRestVertxTransportConfig.class)
    ITJUnitUtils.run(TestRestServerConfigEdge.class);
  }

  private static void testH2Standalone() throws Throwable {
    runShareTestCases();

    //as setMaxInitialLineLength() is not work for http2, do not need
    // ITJUnitUtils.runWithRest(TestRestVertxTransportConfig.class)
    ITJUnitUtils.run(TestRestServerConfigEdge.class);
  }

  private static void testSpringBoot2Standalone() throws Throwable {
    runShareTestCases();

    // currently, only support vertx download
    ITJUnitUtils.run(TestDownloadSlowStreamEdge.class);
  }

  private static void testSpringBoot2Servlet() throws Throwable {
    runShareTestCases();

    TestTransportContext.expectName = HttpTransportContext.class.getName();
    ITJUnitUtils.runWithRest(TestTransportContext.class);
  }
}
