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

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.servicecomb.core.provider.consumer.InvokerUtils;
import io.servicecomb.demo.TestMgr;
import io.servicecomb.provider.pojo.RpcReference;
import io.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年3月22日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component
public class CrossappClient {
    @RpcReference(microserviceName = "appServer:appService", schemaId = "helloworld")
    private static HelloWorld helloWorld;

    public static void main(String[] args) throws Exception {
        Log4jUtils.init();
        BeanUtils.init();

        test();

        TestMgr.summary();
    }

    public static void test() {
        Object result = InvokerUtils.syncInvoke("appServer:appService", "helloworld", "sayHello", null);
        TestMgr.check("hello world", result);

        RestTemplate restTemplate = RestTemplateBuilder.create();
        result = restTemplate.getForObject("cse://appServer:appService/helloworld/hello", String.class);
        TestMgr.check("hello world", result);

        result = helloWorld.sayHello();
        TestMgr.check("hello world", result);
    }
}
