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

import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.it.deploy.Deploys;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.apache.servicecomb.it.testcase.base.TestDataTypePojo;
import org.apache.servicecomb.it.testcase.base.TestDataTypeRest;
import org.apache.servicecomb.it.testcase.support.ProducerDevMode;
import org.apache.servicecomb.transport.highway.HighwayTransport;


public class ConsumerMain {
  private static ResultPrinter resultPrinter = new ResultPrinter();

  private static Deploys deploys = new Deploys();

  private static List<String> transports;

  public static boolean autoExit = true;

  public static void main(String[] args) throws Throwable {
    BeanUtils.init();
    ITUtils.waitBootFinished();

    deploys.init();
    run();

    SCBEngine.getInstance().destroy();
    resultPrinter.print();

    if (autoExit) {
      System.exit(0);
    }
  }

  protected static void run() throws Throwable {
    // deploy edge/zuul
    // if not ready, will start a new instance and wait for ready
    deploys.getEdge().ensureReady();
    // deploys.getZuul().ensureReady(zuul);

    // 1.base test case
    //   include all extension point abnormal scenes test case

    // deploy standalone base-producer
    //   only run one case for "any" transport
    //   run highway
    //   run rest
    //   run native restTemplate to edge/zuul
    // stop standalone base-producer
    transports = Arrays.asList(HighwayTransport.NAME, Const.RESTFUL);
    testStandalone();

    transports = Arrays.asList(Const.RESTFUL);
    // deploy tomcat base-producer
    //   run vertx-servlet
    //   run native restTemplate to edge/zuul
    // stop tomcat base-producer

    // deploy spring boot base-producer
    //   run vertx-servlet
    //   run native restTemplate to edge/zuul
    // stop spring boot base-producer

    // 2.complex test case
    //   1)start new producer version
    //     consumer/edge/zuul should ......
    //   2)delete new producer version
    //     consumer/edge/zuul should ......
    //   ......

    // 3.deploy development mode producer
    // ......

    deploys.getEdge().stop();
  }

  private static void testStandalone() throws Throwable {
    deploys.getBaseProducer().ensureReady();
    ITJUnitUtils.addParent("standalone");

    testDataType();

    ITJUnitUtils.getParents().pop();
    deploys.getBaseProducer().stop();
  }

  private static void testDataType() {
    testDataType(ProducerDevMode.Pojo, TestDataTypePojo.class);
    testDataType(ProducerDevMode.Jaxrs, TestDataTypeRest.class);
    testDataType(ProducerDevMode.Springmvc, TestDataTypeRest.class);

    ITJUnitUtils.getParents().push("edge");
//    runEdge();
    ITJUnitUtils.getParents().pop();

    ITJUnitUtils.getParents().push("zuul");
//    runEdge();
    ITJUnitUtils.getParents().pop();
  }

  private static void testDataType(ProducerDevMode producerDevMode, Class<?>... classes) {
    ITJUnitUtils.addParent(producerDevMode.name());
    for (String transport : transports) {
      ITJUnitUtils.addParent(transport);

      ITUtils.invokeExactStaticMethod(classes, "init", transport, producerDevMode);
      ITJUnitUtils.run(classes);

      ITJUnitUtils.getParents().pop();
    }
    ITJUnitUtils.getParents().pop();
  }
}
