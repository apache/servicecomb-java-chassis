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
package org.apache.servicecomb.samples.pojo.consumer;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.samples.common.schema.Hello;
import org.apache.servicecomb.samples.common.schema.models.Person;
import org.apache.servicecomb.samples.pojo.Compute;
import org.springframework.stereotype.Component;

@Component
public class PojoConsumerMain {

  @RpcReference(microserviceName = "hello", schemaId = "hello")
  private static Hello hello;

  @RpcReference(microserviceName = "hello", schemaId = "codeFirstCompute")
  public static Compute compute;

  public static void main(String[] args)
      throws Exception {
    init();
    System.out.println(hello.sayHi("Java Chassis"));
    Person person = new Person();
    person.setName("ServiceComb/Java Chassis");
    System.out.println(hello.sayHello(person));
    System.out.println("a=1, b=2, result=" + compute.add(1, 2));
  }

  public static void init()
      throws Exception {
    Log4jUtils.init();
    BeanUtils.init();
  }
}
