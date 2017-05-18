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

package com.huawei.paas.cse.demo.client.perf;

import com.huawei.paas.cse.demo.pojo.client.PojoClient;
import com.huawei.paas.foundation.vertx.VertxUtils;

import io.vertx.core.Vertx;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * 
 * @author   
 * @version  [版本号, 2016年12月3日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class PerfClient {
    public static void main(String[] args) throws Exception {
        PojoClient.init();

        System.out.println("mode:" + Config.getMode());

        if ("reactive".equals(Config.getMode())) {
            Vertx vertx = VertxUtils.getOrCreateVertxByName("perfClient", null);
            VertxUtils.deployVerticle(vertx, ClientVerticle.class, Config.getClientThread());
            return;
        }

        for (int idx = 0; idx < Config.getClientThread(); idx++) {
            new ClientThread().start();
        }
    }
}
