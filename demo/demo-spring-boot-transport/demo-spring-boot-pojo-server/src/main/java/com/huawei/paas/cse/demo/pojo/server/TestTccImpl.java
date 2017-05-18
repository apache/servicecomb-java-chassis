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

package com.huawei.paas.cse.demo.pojo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.paas.cse.demo.server.TestRequest;
import com.huawei.paas.cse.demo.server.User;
import com.huawei.paas.cse.demo.tccserver.TestTcc;
import com.huawei.paas.cse.tcc.annotation.TccTransaction;

public class TestTccImpl implements TestTcc {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestTccImpl.class);

    @Override
    @TccTransaction(confirmMethod = "serverConfirm", cancelMethod = "serverCancel")
    public User splitParam(int index, User user) {
        int division = 1 / index;
        LOGGER.info("division is {}", division);
        LOGGER.info("splitParam({}, {}) successfully", index, user);
        return user;
    }

    public User serverConfirm(int index, User user) {
        LOGGER.info("serverConfirm({}, {}) successfully", index, user);
        return user;
    }

    public User serverCancel(int index, User user) {
        LOGGER.info("serverCancel({}, {}) successfully", index, user);
        return user;
    }

    @Override
    public User wrapParam(TestRequest request) {
        return null;
    }
}
