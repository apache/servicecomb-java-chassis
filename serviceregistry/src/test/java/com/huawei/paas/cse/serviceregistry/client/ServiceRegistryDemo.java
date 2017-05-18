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

package com.huawei.paas.cse.serviceregistry.client;

import com.huawei.paas.cse.serviceregistry.RegistryUtils;
import com.huawei.paas.foundation.common.utils.BeanUtils;
import com.huawei.paas.foundation.common.utils.Log4jUtils;

/**
 * Created by   on 2017/1/26.
 */
public class ServiceRegistryDemo {
    public static void main(String[] args) throws Exception {
        Log4jUtils.init();
        BeanUtils.init();

        // 1、自注册 2、服务心跳 3、实例变化监听
        RegistryUtils.init();
    }
}
