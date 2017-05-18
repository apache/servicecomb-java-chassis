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

import org.springframework.beans.factory.InitializingBean;

import com.netflix.config.DynamicPropertyFactory;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年12月5日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class MetricsConfig implements InitializingBean {

    private static final int DEFAULT_METRICS_CYCLE = 60000;

    /**
     * 获取msCycle的值
     * @return 返回 msCycle
     */
    public static int getMsCycle() {
        return DynamicPropertyFactory.getInstance()
                .getIntProperty("cse.metrics.cycle.ms", DEFAULT_METRICS_CYCLE)
                .get();
    }

    /**
     * 获取enable的值
     * @return 返回 enable
     */
    public static boolean isEnable() {
        return DynamicPropertyFactory.getInstance().getBooleanProperty("cse.metrics.enabled", true).get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (!isEnable()) {
            return;
        }

        new MetricsThread().start();
    }

}
