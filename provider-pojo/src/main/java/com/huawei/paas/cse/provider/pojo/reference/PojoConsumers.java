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
package com.huawei.paas.cse.provider.pojo.reference;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.huawei.paas.cse.core.provider.CseBeanPostProcessor.ConsumerFieldProcessor;
import com.huawei.paas.cse.provider.pojo.RpcReference;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author
 * @version  [版本号, 2017年4月28日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component
public class PojoConsumers implements ConsumerFieldProcessor {
    private List<PojoReferenceMeta> consumerList = new ArrayList<>();

    public void addPojoReferenceMeta(PojoReferenceMeta meta) {
        consumerList.add(meta);
    }

    public List<PojoReferenceMeta> getConsumerList() {
        return consumerList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processConsumerField(ApplicationContext applicationContext, Object bean, Field field) {
        RpcReference reference = field.getAnnotation(RpcReference.class);
        if (reference == null) {
            return;
        }

        handleReferenceField(bean, field, reference);
    }

    private void handleReferenceField(Object obj, Field field,
            RpcReference reference) {
        PojoReferenceMeta pojoReference = new PojoReferenceMeta();
        pojoReference.setMicroserviceName(reference.microserviceName());
        pojoReference.setSchemaId(reference.schemaId());
        pojoReference.setConsumerIntf(field.getType());

        pojoReference.afterPropertiesSet();

        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, obj, pojoReference.getProxy());

        addPojoReferenceMeta(pojoReference);
    }
}
