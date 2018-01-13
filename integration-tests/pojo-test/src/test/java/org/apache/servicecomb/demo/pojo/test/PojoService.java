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

package org.apache.servicecomb.demo.pojo.test;

import javax.inject.Inject;

import org.apache.servicecomb.demo.CodeFirstPojoIntf;
import org.apache.servicecomb.demo.helloworld.greeter.Hello;
import org.apache.servicecomb.demo.server.Test;
import org.apache.servicecomb.demo.smartcare.SmartCare;
import org.springframework.stereotype.Component;

@Component
public class PojoService {

  static Hello hello;

  static SmartCare smartCare;

  static org.apache.servicecomb.demo.server.Test test;

  static CodeFirstPojoIntf codeFirst;

  @Inject
  public void setHello(Hello hello) {
    PojoService.hello = hello;
  }

  @Inject
  public void setSmartCare(SmartCare smartCare) {
    PojoService.smartCare = smartCare;
  }

  @Inject
  public void setTest(Test test) {
    PojoService.test = test;
  }

  @Inject
  public void setCodeFirst(CodeFirstPojoIntf codeFirst) {
    PojoService.codeFirst = codeFirst;
  }
}
