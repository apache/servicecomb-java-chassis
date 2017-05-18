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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.paas.cse.demo.server.TestRequest;
import com.huawei.paas.cse.demo.server.User;
import com.huawei.paas.cse.demo.tccserver.TestTcc;
import com.huawei.paas.cse.tcc.annotation.TccTransaction;
import com.huawei.paas.foundation.common.utils.BeanUtils;

/**
 * Created by   on 2016/12/14.
 */
public class TestTccBusiness {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestTccBusiness.class);

    private TestTcc testTcc;

    public void init() {
        this.testTcc = BeanUtils.getBean("tcc-server");
    }

    @TccTransaction(confirmMethod = "clientConfirm", cancelMethod = "clientCancel")
    public User start(int index) {
        User user = new User();
        user.setIndex(index);
        user.setName("haha");
        user.setAge(10);
        TestRequest request = new TestRequest();
        request.setUser(user);

        User result = testTcc.splitParam(index, user);
        LOGGER.info("{}", result);

        return result;
    }

    public User clientConfirm(int index) {
        LOGGER.info("clientConfirm {} successfully", index);
        return null;
    }

    public User clientCancel(int index) {
        LOGGER.info("clientCancel {} successfully", index);
        return null;
    }
}
