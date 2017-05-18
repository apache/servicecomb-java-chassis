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

import java.util.Map;

/**
 * 各种类型的http request接口统一化
 * TODO: 该接口需继续扩展增强，满足更多对request数据的需求
 * @author   
 * @version  [版本号, 2017年1月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public interface RestServerRequest {
    String getPath();

    String getMethod();

    String[] getQueryParam(String key);

    Map<String, String[]> getQueryParams();

    String getPathParam(String key);

    String getHeaderParam(String key);

    Object getFormParam(String key);

    String getCookieParam(String key);

    // 可能是inputStream，也可能直接就是目标Object
    Object getBody() throws Exception;
}
