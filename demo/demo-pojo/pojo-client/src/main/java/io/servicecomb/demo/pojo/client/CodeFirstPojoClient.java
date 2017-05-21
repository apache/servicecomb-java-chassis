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

package io.servicecomb.demo.pojo.client;

import java.util.Arrays;
import java.util.Date;

import javax.inject.Inject;

import io.servicecomb.core.CseContext;
import io.servicecomb.demo.CodeFirstPojoIntf;
import io.servicecomb.demo.DemoConst;
import io.servicecomb.demo.TestMgr;
import io.servicecomb.demo.compute.Person;
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
public class CodeFirstPojoClient {
    @RpcReference(microserviceName = "pojo", schemaId = "com.huawei.paas.cse.demo.CodeFirstPojoIntf")
    public CodeFirstPojoIntf codeFirstAnnotation;

    @RpcReference(microserviceName = "pojo")
    public CodeFirstPojoIntf codeFirstAnnotationEmptySchemaId;

    @Inject
    private CodeFirstPojoIntf codeFirstFromXml;

    public void testCodeFirst(String microserviceName) {
        for (String transport : DemoConst.transports) {
            CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
            TestMgr.setMsg(microserviceName, transport);

            testAll(codeFirstAnnotation, transport);
            testAll(codeFirstAnnotationEmptySchemaId, transport);
            testAll(codeFirstFromXml, transport);
        }
    }

    protected void testAll(CodeFirstPojoIntf codeFirst, String transport) {
        testCodeFirstBytes(codeFirst);
        testCodeFirstAddDate(codeFirst);
        testCodeFirstAddString(codeFirst);
        testCodeFirstIsTrue(codeFirst);
        testCodeFirstSayHi2(codeFirst);
        // grpc没处理非200的场景
        if (!transport.equals("grpc") && !transport.equals("")) {
            testCodeFirstSayHi(codeFirst);
        }
        testCodeFirstSaySomething(codeFirst);
        //            testCodeFirstRawJsonString(template, cseUrlPrefix);
        testCodeFirstSayHello(codeFirst);
        testCodeFirstReduce(codeFirst);
    }

    private void testCodeFirstBytes(CodeFirstPojoIntf codeFirst) {
        byte[] input = new byte[] {0, 1, 2};
        byte[] result = codeFirst.testBytes(input);
        TestMgr.check(3, result.length);
        TestMgr.check(1, result[0]);
        TestMgr.check(1, result[1]);
        TestMgr.check(2, result[2]);
    }

    private void testCodeFirstAddDate(CodeFirstPojoIntf codeFirst) {
        Date date = new Date();
        int seconds = 1;
        Date result = codeFirst.addDate(date, seconds);
        TestMgr.check(new Date(date.getTime() + seconds * 1000), result);
    }

    protected void testCodeFirstAddString(CodeFirstPojoIntf codeFirst) {
        String result = codeFirst.addString(Arrays.asList("a", "b"));
        TestMgr.check("ab", result);
    }

    protected void testCodeFirstIsTrue(CodeFirstPojoIntf codeFirst) {
        boolean result = codeFirst.isTrue();
        TestMgr.check(true, result);
    }

    protected void testCodeFirstSayHi2(CodeFirstPojoIntf codeFirst) {
        String result = codeFirst.sayHi2("world");
        TestMgr.check("world sayhi 2", result);
    }

    protected void testCodeFirstSayHi(CodeFirstPojoIntf codeFirst) {
        String result = codeFirst.sayHi("world");
        TestMgr.check("world sayhi", result);
        //        TestMgr.check(202, responseEntity.getStatusCode());
    }

    protected void testCodeFirstSaySomething(CodeFirstPojoIntf codeFirst) {
        Person person = new Person();
        person.setName("person name");

        String result = codeFirst.saySomething("prefix  prefix", person);
        TestMgr.check("prefix  prefix person name", result);
    }

    protected void testCodeFirstSayHello(CodeFirstPojoIntf codeFirst) {
        Person input = new Person();
        input.setName("person name");

        Person result = codeFirst.sayHello(input);
        TestMgr.check("hello person name", result.getName());
    }

    protected void testCodeFirstReduce(CodeFirstPojoIntf codeFirst) {
        int result = codeFirst.reduce(5, 3);
        TestMgr.check(2, result);
    }
}
