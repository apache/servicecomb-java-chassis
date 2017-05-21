/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.paas.cse.demo.pojo.client;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import io.servicecomb.core.CseContext;
import com.huawei.paas.cse.demo.DemoConst;
import com.huawei.paas.cse.demo.TestMgr;
import com.huawei.paas.cse.demo.compute.Person;
import com.huawei.paas.cse.provider.pojo.RpcReference;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author   
 * @version  [版本号, 2017年4月10日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component
public class CodeFirstPojoClient {
    @RpcReference(microserviceName = "pojo", schemaId = "codeFirst")
    public CodeFirstPojoIntf codeFirst;

    public void testCodeFirst(String microserviceName) {
        for (String transport : DemoConst.transports) {
            CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
            TestMgr.setMsg(microserviceName, transport);

            testCodeFirstAddString();
            testCodeFirstIsTrue();
            testCodeFirstSayHi2();
            // grpc没处理非200的场景
            if (!transport.equals("grpc") && !transport.equals("")) {
                testCodeFirstSayHi();
            }
            testCodeFirstSaySomething();
            //            testCodeFirstRawJsonString(template, cseUrlPrefix);
            testCodeFirstSayHello();
            testCodeFirstReduce();
        }
    }

    protected void testCodeFirstAddString() {
        String result = codeFirst.addString(Arrays.asList("a", "b"));
        TestMgr.check("ab", result);
    }

    protected void testCodeFirstIsTrue() {
        boolean result = codeFirst.isTrue();
        TestMgr.check(true, result);
    }

    protected void testCodeFirstSayHi2() {
        String result = codeFirst.sayHi2("world");
        TestMgr.check("world sayhi 2", result);
    }

    protected void testCodeFirstSayHi() {
        String result = codeFirst.sayHi("world");
        TestMgr.check("world sayhi", result);
        //        TestMgr.check(202, responseEntity.getStatusCode());
    }

    protected void testCodeFirstSaySomething() {
        Person person = new Person();
        person.setName("person name");

        String result = codeFirst.saySomething("prefix  prefix", person);
        TestMgr.check("prefix  prefix person name", result);
    }

    protected void testCodeFirstSayHello() {
        Person input = new Person();
        input.setName("person name");

        Person result = codeFirst.sayHello(input);
        TestMgr.check("hello person name", result.getName());
    }

    protected void testCodeFirstReduce() {
        int result = codeFirst.reduce(5, 3);
        TestMgr.check(2, result);
    }
}
