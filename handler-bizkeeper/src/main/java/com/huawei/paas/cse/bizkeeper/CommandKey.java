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

package com.huawei.paas.cse.bizkeeper;

import com.huawei.paas.cse.core.Invocation;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;

/**
 * 创建对应的Key值
 * @author   
 * @version  [版本号, 2016年12月17日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public final class CommandKey {
    private CommandKey() {
    }

    public static HystrixCommandGroupKey toHystrixCommandGroupKey(String type, Invocation invocation) {
        return HystrixCommandGroupKey.Factory
                .asKey(type + "." + invocation.getOperationMeta().getMicroserviceQualifiedName());
    }

    public static HystrixCommandKey toHystrixCommandKey(String type, Invocation invocation) {
        return HystrixCommandKey.Factory
                .asKey(type + "." + invocation.getOperationMeta().getMicroserviceQualifiedName());
    }
}
