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

package com.huawei.paas.foundation.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.paas.foundation.common.CommonThread;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年12月5日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class MetricsThread extends CommonThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsThread.class);

    private static final long SECOND_MILLS = 1000;

    // 从进程启动开始，每秒加1000
    // 这不是一个精确值，用于不关注精度的超时检测
    private static long msTick = 0;

    /**
     * <构造函数> [参数说明]
     */
    public MetricsThread() {
        setName("metrics");
    }

    @Override
    public void run() {
        while (isRunning()) {
            waitOneCycle();

            try {
                Metrics.onCycle();
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
            }
        }
    }

    private void waitOneCycle() {
        long msLastCycle = msTick;
        for (;;) {
            try {
                sleep(SECOND_MILLS);
            } catch (InterruptedException e) {
            }
            msTick += SECOND_MILLS;

            if (msTick - msLastCycle >= MetricsConfig.getMsCycle()) {
                break;
            }
        }
    }

    public static long getMsTick() {
        return msTick;
    }
}
