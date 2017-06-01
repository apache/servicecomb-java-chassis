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

package io.servicecomb.swagger.invocation;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @version  [版本号, 2017年2月13日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class SwaggerInvocationContext {
    // value只能是简单类型
    protected Map<String, String> context = new HashMap<>();

    public SwaggerInvocationContext() {

    }

    public SwaggerInvocationContext(Map<String, String> context) {
        this.context = context;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public void addContext(String key, String value) {
        context.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getContext(String key) {
        return (T) context.get(key);
    }

    public void addContext(Map<String, String> otherContext) {
        if (otherContext == null) {
            return;
        }

        context.putAll(otherContext);
    }
}
