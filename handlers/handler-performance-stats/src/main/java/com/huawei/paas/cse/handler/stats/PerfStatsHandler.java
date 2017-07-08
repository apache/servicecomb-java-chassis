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

package com.huawei.paas.cse.handler.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.handler.impl.AbstractHandler;
import io.servicecomb.foundation.metrics.Metrics;
import io.servicecomb.foundation.metrics.performance.PerfStatContext;
import io.servicecomb.foundation.metrics.performance.PerfStatSuccFail;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.swagger.invocation.exception.InvocationException;

/**
 * 统计成功、失败的tps、时延等等参数
 * @author   
 * @version  [版本号, 2016年12月5日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class PerfStatsHandler extends AbstractHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerfStatsHandler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
        String name = invocation.getOperationMeta().getMicroserviceQualifiedName();
        // 先确认是否需要跟踪?
        if (!isNeedPerfStat(name)) {
            invocation.next(asyncResp);
            return;
        }

        PerfStatContext context = new PerfStatContext();

        // TODO:
        // 1.如果能确定是放在exceptionHandler前面，则这里不必catch
        // 2.作为handler，框架上的异常场景统计不到
        // 3.这里只统计了调用的tps，而业务层的消息数，如何感知支撑？
        // 使用标准的metrics方案，可以解决这些问题
        try {
            invocation.next(resp -> {
                asyncResp.handle(resp);

                onStat(invocation, resp.isSuccessed(), context);
            });
        } catch (Throwable e) {
            if (!InvocationException.class.isInstance(e)) {
                LOGGER.error("invoke failed,", e);
            }
            onStat(invocation, false, context);
            asyncResp.fail(invocation.getInvocationType(), e);
        }
    }

    protected void onStat(Invocation invocation, boolean isSucc, PerfStatContext context) {
        context.setMsgCount(1);

        // 统计标识带上transport名称，方便识别场景
        String statName = invocation.getInvocationQualifiedName();

        // TODO:同优先级的，后加入的应该在最后
        PerfStatSuccFail stat = Metrics.getOrCreateLocalPerfStat(statName, 0);
        stat.add(isSucc, context);
    }

    // TODO:由客户端带过来？不应该要求两端都配置？
    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param name
     * @return
     */
    private boolean isNeedPerfStat(String name) {
        return true;
    }

}
