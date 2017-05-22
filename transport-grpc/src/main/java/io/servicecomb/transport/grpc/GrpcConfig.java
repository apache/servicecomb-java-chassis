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

package io.servicecomb.transport.grpc;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

public final class GrpcConfig {
    private GrpcConfig() {
    }

    /**
     * 获取address的值
     * @return 返回 address
     */
    public static String getAddress() {
        DynamicStringProperty address =
            DynamicPropertyFactory.getInstance().getStringProperty("cse.grpc.address", null);
        return address.get();
    }

    /**
     * 获取threadCount的值
     * @return 返回 threadCount
     */
    public static int getThreadCount() {
        DynamicIntProperty address =
            DynamicPropertyFactory.getInstance().getIntProperty("cse.grpc.thread-count", 1);
        return address.get();
    }

    /**
     * 获取connectionPoolPerThread的值
     * @return 返回 connectionPoolPerThread
     */
    public static int getConnectionPoolPerThread() {
        DynamicIntProperty address =
            DynamicPropertyFactory.getInstance().getIntProperty("cse.grpc.connection-pool-per-thread", 1);
        return address.get();
    }
}
