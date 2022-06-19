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

package org.apache.servicecomb.it.schema.generic;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.it.Consumers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class TestMyService {
  private static final Consumers<IMyService> myService = new Consumers<>("MyEndpoint",
      IMyService.class);

  private static final Consumers<IMyService> myServiceWithInterface = new Consumers<>("MyEndpointWithInterface",
      IMyService.class);

  @Test
  public void testServiceInoke() {
    PersonBean bean = new PersonBean();
    bean.setName("p");
    PersonBean resultBean = myService.getIntf().hello(bean);
    Assertions.assertEquals("p", resultBean.getName());

    resultBean = myService.getIntf().hello(bean, "p");
    Assertions.assertEquals("p:p", resultBean.getName());

    resultBean = myService.getIntf().actual();
    Assertions.assertEquals("p", resultBean.getName());

    resultBean = myService.getIntf().objectParam("p");
    Assertions.assertEquals("p", resultBean.getName());

    resultBean = myService.getIntf().objectParamTwo("p", "p");
    Assertions.assertEquals("p:p", resultBean.getName());

    PersonBean[] beanArray = new PersonBean[] {bean};
    PersonBean[] beanArrayResult = myService.getIntf().helloBody(beanArray);
    Assertions.assertEquals("p", beanArrayResult[0].getName());

    List<PersonBean> beanList = new ArrayList<>();
    beanList.add(bean);
    List<PersonBean> beanListResult = myService.getIntf().helloList(beanList);
    Assertions.assertEquals("p", beanListResult.get(0).getName());
  }

  @Test
  public void testServiceWithInterfaceInoke() {
    PersonBean bean = new PersonBean();
    bean.setName("p");
    PersonBean resultBean = myServiceWithInterface.getIntf().hello(bean);
    Assertions.assertEquals("p", resultBean.getName());

    resultBean = myServiceWithInterface.getIntf().hello(bean, "p");
    Assertions.assertEquals("p:p", resultBean.getName());

    resultBean = myServiceWithInterface.getIntf().actual();
    Assertions.assertEquals("p", resultBean.getName());

    resultBean = myServiceWithInterface.getIntf().objectParam("p");
    Assertions.assertEquals("p", resultBean.getName());

    resultBean = myServiceWithInterface.getIntf().objectParamTwo("p", "p");
    Assertions.assertEquals("p:p", resultBean.getName());

    PersonBean[] beanArray = new PersonBean[] {bean};
    PersonBean[] beanArrayResult = myServiceWithInterface.getIntf().helloBody(beanArray);
    Assertions.assertEquals("p", beanArrayResult[0].getName());

    List<PersonBean> beanList = new ArrayList<>();
    beanList.add(bean);
    List<PersonBean> beanListResult = myServiceWithInterface.getIntf().helloList(beanList);
    Assertions.assertEquals("p", beanListResult.get(0).getName());
  }
}
