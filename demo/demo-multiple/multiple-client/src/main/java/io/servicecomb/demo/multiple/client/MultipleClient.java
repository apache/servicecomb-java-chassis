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
package io.servicecomb.demo.multiple.client;

import io.servicecomb.demo.TestMgr;
import io.servicecomb.demo.crossapp.CrossappClient;
import io.servicecomb.demo.jaxrs.client.JaxrsClient;
import io.servicecomb.demo.pojo.client.PojoClient;
import io.servicecomb.demo.springmvc.client.SpringmvcClient;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;

public class MultipleClient {
    public static void main(String[] args) throws Exception {
        Log4jUtils.init();
        BeanUtils.init();

        PojoClient.run();
        JaxrsClient.run();
        SpringmvcClient.run();
        CrossappClient.run();

        TestMgr.summary();
    }
}
