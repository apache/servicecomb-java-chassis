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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.exception.CommonExceptionData;
import io.servicecomb.core.exception.InvocationException;
import io.servicecomb.core.provider.consumer.InvokerUtils;
import io.servicecomb.demo.DemoConst;
import io.servicecomb.demo.TestMgr;
import io.servicecomb.demo.server.Test;
import io.servicecomb.demo.server.TestRequest;
import io.servicecomb.demo.server.User;
import io.servicecomb.demo.smartcare.Application;
import io.servicecomb.demo.smartcare.Group;
import io.servicecomb.demo.smartcare.SmartCare;
import io.servicecomb.provider.pojo.RpcReference;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author  
 * @version [版本号, 2017年1月3日]
 * @see  [相关类/方法]
 * @since [产品/模块版本]
 */
@Component(value = "normalPojoClient")
public class PojoClient {
    private static Logger LOGGER = LoggerFactory.getLogger(PojoClient.class);

    public static CodeFirstPojoClient codeFirstPojoClient;

    @RpcReference(microserviceName = "pojo")
    public static Test test;

    private static SmartCare smartcare;

    private static TestTccBusiness testTccBusiness;

    public static final byte buffer[] = new byte[1024];

    static {
        Arrays.fill(buffer, (byte) 1);
    }

    @Inject
    public void setCodeFirstPojoClient(CodeFirstPojoClient codeFirstPojoClient) {
        PojoClient.codeFirstPojoClient = codeFirstPojoClient;
    }

    public static void main(String[] args) throws Exception {
        init();

        runTest();
    }

    public static void init() throws Exception {
        Log4jUtils.init();
        BeanUtils.init();
    }

    public static void runTest() throws Exception {
        smartcare = BeanUtils.getBean("smartcare");

        testTccBusiness = BeanUtils.getBean("testTccBusiness");
        testTccBusiness.init();

        String microserviceName = "pojo";
        codeFirstPojoClient.testCodeFirst(microserviceName);

        for (String transport : DemoConst.transports) {
            CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
            TestMgr.setMsg(microserviceName, transport);
            LOGGER.info("test {}, transport {}", microserviceName, transport);

            testNull(test);
            testEmpty(test);
            testStringArray(test);
            testChinese(test);
            testStringHaveSpace(test);
            testWrapParam(test);
            testSplitParam(test);
            testInputArray(test);

            // 异常，当前只有grpc不支持
            if (!transport.equals("grpc") && !transport.equals("")) {
                testException(test);

                testTcc(testTccBusiness);
            }

            testSmartCare(smartcare);

            testCommonInvoke(transport);
        }

        TestMgr.summary();
    }

    private static void testSmartCare(SmartCare smartCare) {
        Group group = new Group();
        group.setName("group0");

        Application application = new Application();
        application.setName("app0");
        application.setDefaultGroup("group0");
        application.setVersion("v1");
        application.setDynamicFlag(true);
        List<Group> groups = new ArrayList<>();
        groups.add(group);
        application.setGroups(groups);

        TestMgr.check("resultCode: 0\nresultMessage: add application app0 success",
                smartCare.addApplication(application));
        TestMgr.check("resultCode: 1\nresultMessage: delete application app0 failed",
                smartCare.delApplication("app0"));
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param test
     */
    private static void testException(Test test) {
        try {
            test.testException(456);
        } catch (InvocationException e) {
            TestMgr.check("456 error", e.getErrorData());
        }

        try {
            test.testException(556);
        } catch (InvocationException e) {
            TestMgr.check("[556 error]", e.getErrorData());
        }

        try {
            test.testException(557);
        } catch (InvocationException e) {
            TestMgr.check("[[557 error]]", e.getErrorData());
        }
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     */
    private static void testTcc(TestTccBusiness testTccBusiness) {
        User result = testTccBusiness.start(1);
        TestMgr.check("User [name=haha, age=10, index=1]", result);

        try {
            result = testTccBusiness.start(0);
        } catch (InvocationException e) {
            TestMgr.check("Cse Internal Server Error", ((CommonExceptionData) e.getErrorData()).getMessage());
        }
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     */
    private static void testInputArray(Test test) {
        String result = test.addString(new String[] {"a", "b"});
        LOGGER.info("input array result:{}", result);
        TestMgr.check("[a, b]", result);
    }

    private static void testSplitParam(Test test) {
        User result = test.splitParam(1, new User());
        LOGGER.info("split param result:{}", result);
        TestMgr.check("User [name=nameA,  users count:0, age=100, index=1]", result);
    }

    private static void testCommonInvoke(String transport) {
        Object result = InvokerUtils.syncInvoke("pojo", "server", "splitParam", new Object[] {2, new User()});
        TestMgr.check("User [name=nameA,  users count:0, age=100, index=2]", result);

        result =
            InvokerUtils.syncInvoke("pojo",
                    "0.0.1",
                    transport,
                    "server",
                    "splitParam",
                    new Object[] {3, new User()});
        TestMgr.check("User [name=nameA,  users count:0, age=100, index=3]", result);
    }

    private static void testEmpty(Test test) {
        TestMgr.check("code is ''", test.getTestString(""));
    }

    private static void testNull(Test test) {
        TestMgr.check("code is 'null'", test.getTestString(null));
        TestMgr.check(null, test.wrapParam(null));
    }

    private static void testChinese(Test test) {
        TestMgr.check("code is '测试'", test.getTestString("测试"));

        User user = new User();
        user.setName("名字");
        User result = test.splitParam(1, user);
        TestMgr.check("名字,  users count:0", result.getName());
    }

    private static void testStringHaveSpace(Test test) {
        TestMgr.check("code is 'a b'", test.getTestString("a b"));
    }

    private static void testStringArray(Test test) {
        //        TestMgr.check("arr is '[a, , b]'", test.testStringArray(new String[] {"a", null, "b"}));
        TestMgr.check("arr is '[a, b]'", test.testStringArray(new String[] {"a", "b"}));
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     */
    private static void testWrapParam(Test test) {
        User user = new User();

        TestRequest request = new TestRequest();
        request.setUser(user);
        request.setIndex(0);
        request.setData(buffer);
        request.getUsers().add(user);

        User result = test.wrapParam(request);
        LOGGER.info("wrap param result:{}", result);

        TestMgr.check("User [name=nameA,  users count:1, age=100, index=0]", result);
    }

}
