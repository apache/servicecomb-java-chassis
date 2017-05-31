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

package io.servicecomb.transport.rest.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletResponse;

/**
 * 无状态
 *
 * @version  [版本号, 2017年1月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class RestAsyncListener implements AsyncListener {
    /**
     * {@inheritDoc}
     */
    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        // 未使用
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        // TODO:超时的处理，要重新考虑
        ServletResponse response = event.getAsyncContext().getResponse();
        PrintWriter out = response.getWriter();
        out.write("TimeOut Error in Processing");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(AsyncEvent event) throws IOException {
        // 未使用
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        // 未使用
    }

}
