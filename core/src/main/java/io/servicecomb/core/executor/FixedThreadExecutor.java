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

package io.servicecomb.core.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年2月22日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class FixedThreadExecutor implements Executor {
    private List<Executor> executorList = new ArrayList<>();

    private AtomicInteger index = new AtomicInteger();

    private Map<Long, Executor> threadExectorMap = new ConcurrentHashMap<>();

    /**
     * <构造函数> [参数说明]
     */
    public FixedThreadExecutor() {
        executorList.add(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        executorList.add(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Runnable command) {
        long threadId = Thread.currentThread().getId();
        Executor executor = threadExectorMap.get(threadId);
        if (executor == null) {
            int idx = index.getAndIncrement() % executorList.size();
            executor = executorList.get(idx);
            threadExectorMap.put(threadId, executor);
        }

        executor.execute(command);
    }
}
