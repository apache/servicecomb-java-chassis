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
package com.huawei.paas.cse.swagger.generator.core.processor.annotation.models;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author
 * @version  [版本号, 2017年4月20日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ResponseConfigBase {
    private String description;

    private String responseReference;

    private Class<?> responseClass;

    private String responseContainer;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResponseReference() {
        return responseReference;
    }

    public void setResponseReference(String responseReference) {
        this.responseReference = responseReference;
    }

    public Class<?> getResponseClass() {
        return responseClass;
    }

    public void setResponseClass(Class<?> responseClass) {
        this.responseClass = responseClass;
    }

    public String getResponseContainer() {
        return responseContainer;
    }

    public void setResponseContainer(String responseContainer) {
        this.responseContainer = responseContainer;
    }

    @Override
    public String toString() {
        return "ResponseConfigBase [description=" + description + ", responseReference=" + responseReference
                + ", responseClass=" + responseClass + ", responseContainer=" + responseContainer + "]";
    }
}
