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

package com.huawei.paas.cse.common.rest.codec;

import io.vertx.core.buffer.Buffer;

/**
 * vertx的HttpClientRequest没有getHeader的能力
 * 在写cookie参数时，没办法多次添加cookie，所以只能进行接口包装
 * @author   
 * @version  [版本号, 2017年1月23日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public interface RestClientRequest {

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param bodyBuffer
     */
    void write(Buffer bodyBuffer);

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @throws Exception
     */
    void end() throws Exception;

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param name
     * @param value
     */
    void addCookie(String name, String value);

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param name
     * @param value
     */
    void putHeader(String name, String value);

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param name
     * @param value
     */
    void addForm(String name, Object value);

}
