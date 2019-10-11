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
import org.junit.Assert;
import org.junit.Test;

public class TestMyService {
  private static Consumers<IMyService> myservice = new Consumers<>("MyEndpoint",
      IMyService.class);

  private static Consumers<IMyService> myserviceWithInterface = new Consumers<>("MyEndpointWithInterface",
      IMyService.class);

  @Test
  public void testServiceInoke() {
    PersonBean bean = new PersonBean();
    bean.setName("p");
    PersonBean resultBean = myservice.getIntf().hello(bean);
    Assert.assertEquals("p", resultBean.getName());

    resultBean = myservice.getIntf().hello(bean, "p");
    Assert.assertEquals("p:p", resultBean.getName());

    resultBean = myservice.getIntf().actual();
    Assert.assertEquals("p", resultBean.getName());

    resultBean = myservice.getIntf().objectParam("p");
    Assert.assertEquals("p", resultBean.getName());

    resultBean = myservice.getIntf().objectParamTwo("p", "p");
    Assert.assertEquals("p:p", resultBean.getName());

    PersonBean[] beanArray = new PersonBean[] {bean};
    PersonBean[] beanArrayResult = myservice.getIntf().helloBody(beanArray);
    Assert.assertEquals("p", beanArrayResult[0].getName());

    List<PersonBean> beanList = new ArrayList<>();
    beanList.add(bean);
    List<PersonBean> beanListResult = myservice.getIntf().helloList(beanList);
    Assert.assertEquals("p", beanListResult.get(0).getName());
  }

  @Test
  public void testServiceWithInterfaceInoke() {
    PersonBean bean = new PersonBean();
    bean.setName("p");
    PersonBean resultBean = myserviceWithInterface.getIntf().hello(bean);
    Assert.assertEquals("p", resultBean.getName());

    resultBean = myserviceWithInterface.getIntf().hello(bean, "p");
    Assert.assertEquals("p:p", resultBean.getName());

    resultBean = myserviceWithInterface.getIntf().actual();
    Assert.assertEquals("p", resultBean.getName());

    resultBean = myserviceWithInterface.getIntf().objectParam("p");
    Assert.assertEquals("p", resultBean.getName());

    resultBean = myserviceWithInterface.getIntf().objectParamTwo("p", "p");
    Assert.assertEquals("p:p", resultBean.getName());

    PersonBean[] beanArray = new PersonBean[] {bean};
    PersonBean[] beanArrayResult = myserviceWithInterface.getIntf().helloBody(beanArray);
    Assert.assertEquals("p", beanArrayResult[0].getName());

    List<PersonBean> beanList = new ArrayList<>();
    beanList.add(bean);
    List<PersonBean> beanListResult = myserviceWithInterface.getIntf().helloList(beanList);
    Assert.assertEquals("p", beanListResult.get(0).getName());
  }
}
