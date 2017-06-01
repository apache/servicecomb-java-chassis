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

package io.servicecomb.swagger.converter.model;

import com.fasterxml.jackson.databind.JavaType;
import io.servicecomb.swagger.converter.ConverterMgr;

import io.swagger.models.RefModel;
import io.swagger.models.Swagger;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 *
 * @version  [版本号, 2017年3月20日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class RefModelConverter extends AbstractModelConverter {
    /**
     * {@inheritDoc}
     */
    @Override
    public JavaType doConvert(ClassLoader classLoader, String packageName, Swagger swagger, Object model) {
        RefModel refModel = (RefModel) model;

        return ConverterMgr.findByRef(classLoader, packageName, swagger, refModel.getSimpleRef());
    }
}
