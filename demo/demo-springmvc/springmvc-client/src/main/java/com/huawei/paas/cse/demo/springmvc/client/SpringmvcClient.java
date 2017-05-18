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

package com.huawei.paas.cse.demo.springmvc.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.huawei.paas.cse.core.CseContext;
import com.huawei.paas.cse.demo.DemoConst;
import com.huawei.paas.cse.demo.TestMgr;
import com.huawei.paas.cse.demo.controller.Controller;
import com.huawei.paas.cse.demo.controller.Person;
import com.huawei.paas.cse.provider.springmvc.reference.RestTemplateBuilder;
import com.huawei.paas.foundation.common.utils.BeanUtils;
import com.huawei.paas.foundation.common.utils.Log4jUtils;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * 
 * @author   
 * @version  [版本号, 2017年1月3日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class SpringmvcClient {
    private static RestTemplate restTemplate;

    private static Controller controller;

    public static void main(String[] args) throws Exception {
        init();

        run();
    }

    public static void run() throws Exception {
        restTemplate = RestTemplateBuilder.create();
        controller = BeanUtils.getBean("controller");

        CodeFirstRestTemplateSpringmvc codeFirstClient =
            BeanUtils.getContext().getBean(CodeFirstRestTemplateSpringmvc.class);
        codeFirstClient.testCodeFirst(restTemplate, "springmvc", "/codeFirstSpringmvc/");
        runTest();

        TestMgr.summary();
    }

    public static void init() throws Exception {
        Log4jUtils.init();
        BeanUtils.init();
    }

    public static void runTest() throws Exception {
        String microserviceName = "springmvc";
        for (String transport : DemoConst.transports) {
            CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
            TestMgr.setMsg(microserviceName, transport);

            testController(restTemplate, microserviceName);

            testController();
        }
    }

    private static void testController(RestTemplate template, String microserviceName) {
        String prefix = "cse://" + microserviceName;

        TestMgr.check("hi world [world]",
                template.getForObject(prefix + "/controller/sayhi?name=world",
                        String.class));

        TestMgr.check("hi world1 [world1]",
                template.getForObject(prefix + "/controller/sayhi?name={name}",
                        String.class,
                        "world1"));

        Map<String, String> params = new HashMap<>();
        params.put("name", "world2");
        TestMgr.check("hi world2 [world2]",
                template.getForObject(prefix + "/controller/sayhi?name={name}",
                        String.class,
                        params));

        TestMgr.check("hello world",
                template.postForObject(prefix + "/controller/sayhello/{name}",
                        null,
                        String.class,
                        "world"));

        HttpHeaders headers = new HttpHeaders();
        headers.add("name", "world");
        @SuppressWarnings("rawtypes")
        HttpEntity entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = template.exchange(prefix + "/controller/sayhei",
                HttpMethod.GET,
                entity,
                String.class);
        TestMgr.check("hei world", response.getBody());

        Person user = new Person();
        user.setName("world");
        TestMgr.check("ha world",
                template.postForObject(prefix + "/controller/saysomething?prefix={prefix}",
                        user,
                        String.class,
                        "ha"));
    }

    private static void testController() {
        TestMgr.check("hi world [world]", controller.sayHi("world"));
        Person user = new Person();
        user.setName("world");
        TestMgr.check("ha world", controller.saySomething("ha", user));
    }
}
