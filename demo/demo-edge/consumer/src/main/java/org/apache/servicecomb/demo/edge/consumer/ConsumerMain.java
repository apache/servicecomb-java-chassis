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

package org.apache.servicecomb.demo.edge.consumer;

import org.apache.servicecomb.demo.edge.model.ChannelRequestBase;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;

public class ConsumerMain {
  public static void main(String[] args) throws Exception {
    Log4jUtils.init();
    BeanUtils.init();

    new Consumer().testEncrypt();
    new Consumer().invokeBusiness("cse://business/business/v1", new ChannelRequestBase());

    System.out.println("Running api dispatcher.");
    new Consumer().run("api");
    System.out.println("Running rest dispatcher.");
    new Consumer().run("rest");
    System.out.println("Running url dispatcher.");
    new Consumer().run("url");
    System.out.println("Running http dispatcher.");
    new Consumer().run("http");

    System.out.println("All test case finished.");
  }
}
