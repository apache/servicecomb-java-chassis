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

package io.servicecomb.qps;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年2月23日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class QpsController {
    private String key;

    private Integer qpsLimit;

    // 本周期的开始时间
    private volatile long msCycleBegin;

    // 到目前为止的请求数
    private AtomicLong requestCount = new AtomicLong();

    // 本周期之前的请求数
    private volatile long lastRequestCount = 0;

    private static final int CYCLE_LENGTH = 1000;

    /**
     * <构造函数> [参数说明]
     */
    public QpsController(String key, Integer qpsLimit) {
        this.key = key;
        this.qpsLimit = qpsLimit;
        this.msCycleBegin = System.currentTimeMillis();
    }

    /**
     * 获取key的值
     * @return 返回 key
     */
    public String getKey() {
        return key;
    }

    /**
     * 获取qpsLimit的值
     * @return 返回 qpsLimit
     */
    public Integer getQpsLimit() {
        return qpsLimit;
    }

    /**
     * 对qpsLimit进行赋值
     * @param qpsLimit qpsLimit的新值
     */
    public void setQpsLimit(Integer qpsLimit) {
        this.qpsLimit = qpsLimit;
    }

    // 返回true，表示需要被控制
    public boolean isLimitNewRequest() {
        long newCount = requestCount.incrementAndGet();
        long msNow = System.currentTimeMillis();
        if (msNow - msCycleBegin > CYCLE_LENGTH) {
            // 新周期
            // 会有多线程竞争，互相覆盖的问题，不过无所谓，不会有什么后果
            lastRequestCount = newCount;
            msCycleBegin = msNow;
        }

        // 配置更新与配置使用是多线程并发的
        // 所以可能operation级别刚刚更新为null
        // 还没来得及修改为引用schema级别或是microservice级别，其他线程还在使用，所以需要规避
        int limitValue = (qpsLimit == null) ? Integer.MAX_VALUE : qpsLimit;
        return newCount - lastRequestCount >= limitValue;
    }
}
