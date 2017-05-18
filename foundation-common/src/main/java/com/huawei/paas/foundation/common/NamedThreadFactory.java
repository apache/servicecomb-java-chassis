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

package com.huawei.paas.foundation.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 已命名线程工厂.
 * 获取新的名字有固定前缀的线程工厂.
 * @author   
 * @version  [版本号, 2016年3月28日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class NamedThreadFactory implements ThreadFactory {
    /**
     * 定义一个AtomicInteger
     */
    private final AtomicInteger threadNumber = new AtomicInteger();

    /**
     * 线程名的前缀
     */
    private String prefix;

    /**
     * <构造函数> [参数说明]
     */
    public NamedThreadFactory() {
        this("Thread");
    }

    /**
     *<默认构造函数>
     * @param prefix    线程名的前缀
     */
    public NamedThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 获取新的名字以prefix为前缀的线程
     * @param r 线程的Runnable对象
     * @return  新的线程
     * @see ThreadFactory#newThread(Runnable)
     */
    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, prefix + "-" + threadNumber.getAndIncrement());
    }

    /**
     * 获得prefix的值
     * @return 返回 prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 对prefix进行赋值
     * @param prefix prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
