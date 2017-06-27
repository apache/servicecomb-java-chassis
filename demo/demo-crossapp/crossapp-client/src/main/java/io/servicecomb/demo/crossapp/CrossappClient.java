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

package io.servicecomb.demo.crossapp;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.servicecomb.core.provider.consumer.InvokerUtils;
import io.servicecomb.demo.TestMgr;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;
import io.servicecomb.provider.springmvc.reference.RestTemplateBuilder;

@Component
public class CrossappClient {
    private static HelloWorld helloWorld;

    private static String targetAppId;

    @Inject
    public void setHelloWorld(HelloWorld helloWorld) {
        CrossappClient.helloWorld = helloWorld;
    }

    @Value("${target.appid}")
    public void setTargetAppId(String targetAppId) {
        CrossappClient.targetAppId = targetAppId;
    }

    public static void main(String[] args) throws Exception {
        Log4jUtils.init();
        BeanUtils.init();

        run();

        TestMgr.summary();
    }

    public static void run() {
        Object result = InvokerUtils.syncInvoke(targetAppId + ":appService", "helloworld", "sayHello", null);
        TestMgr.check("hello world", result);

        RestTemplate restTemplate = RestTemplateBuilder.create();
        result = restTemplate.getForObject("cse://" + targetAppId + ":appService/helloworld/hello", String.class);
        TestMgr.check("hello world", result);

        result = helloWorld.sayHello();
        TestMgr.check("hello world", result);
    }
}
