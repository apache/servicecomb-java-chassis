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

import com.huawei.paas.cse.core.CseContext;
import com.huawei.paas.cse.demo.pojo.client.PojoClient;
import com.huawei.paas.cse.demo.server.Test;
import com.huawei.paas.cse.demo.server.TestRequest;
import com.huawei.paas.cse.demo.server.User;
import com.huawei.paas.foundation.common.CommonThread;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * 
 * @author   
 * @version  [版本号, 2016年12月3日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ClientThread extends CommonThread {
    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Test test = PojoClient.test;
        CseContext.getInstance().getConsumerProviderManager().setTransport("pojo", Config.getTransport());

        System.out.printf("test %s performance\n", Config.getTransport());

        while (isRunning()) {
            int idx = 0;
            for (;;) {
                User user = new User();

                TestRequest request = new TestRequest();
                request.setUser(user);
                request.setIndex(idx);
                request.setData(PojoClient.buffer);

                try {
                    User result = test.wrapParam(request);

                    if (result.getIndex() != idx) {
                        System.out.printf("error result:%s, expect idx %d\n", result, idx);
                    }
                } catch (Throwable e) {
                    //                    e.printStackTrace();
                }
            }
        }
    }
}
