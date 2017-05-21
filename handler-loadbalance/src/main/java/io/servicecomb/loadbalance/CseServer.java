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

package io.servicecomb.loadbalance;

import io.servicecomb.core.Endpoint;
import io.servicecomb.core.Transport;
import com.huawei.paas.cse.serviceregistry.api.registry.MicroserviceInstance;
import com.huawei.paas.cse.serviceregistry.cache.CacheEndpoint;
import com.netflix.loadbalancer.Server;

/**
 * 服务器抽象，address只有transport识别， LB模块不识别
 * LB模块不提供服务器状态监测，这块功能是由注册中心进行处理的。
 * @author   
 * @version  [版本号, 2016年12月29日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class CseServer extends Server {
    private final Endpoint endpoint;

    // 所属服务实例
    private final MicroserviceInstance instance;

    private long lastVisitTime = System.currentTimeMillis();

    public long getLastVisitTime() {
        return lastVisitTime;
    }

    public void setLastVisitTime(long lastVisitTime) {
        this.lastVisitTime = lastVisitTime;
    }

    public CseServer(Transport transport, CacheEndpoint cacheEndpoint) {
        super(null);

        endpoint = new Endpoint(transport, cacheEndpoint.getEndpoint());
        instance = cacheEndpoint.getInstance();

        this.setAlive(true);
        this.setReadyToServe(true);
    }

    /**
     * 获取endpoint的值
     * @return 返回 endpoint
     */
    public Endpoint getEndpoint() {
        return endpoint;
    }

    public MicroserviceInstance getInstance() {
        return instance;
    }

    public String toString() {
        return endpoint.getEndpoint();
    }

    // used in LoadBalancerContext
    public String getHost() {
        return endpoint.getEndpoint();
    }

    public boolean equals(Object o) {
        if (o instanceof CseServer) {
            return this.getHost().equals(((CseServer) o).getHost());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getHost().hashCode();
    }
}
