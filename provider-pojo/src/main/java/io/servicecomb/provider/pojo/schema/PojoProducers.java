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
package io.servicecomb.provider.pojo.schema;

import java.util.Collection;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.servicecomb.core.provider.CseBeanPostProcessor.ProviderProcessor;
import io.servicecomb.foundation.common.RegisterManager;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.provider.pojo.RpcSchema;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author
 * @version  [版本号, 2017年4月28日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component
public class PojoProducers implements ProviderProcessor {
    // key为schemaId
    private RegisterManager<String, PojoProducerMeta> pojoMgr = new RegisterManager<>("pojo service manager");

    public void registerPojoProducer(PojoProducerMeta pojoProducer) {
        pojoMgr.register(pojoProducer.getSchemaId(), pojoProducer);
    }

    public Collection<PojoProducerMeta> getProcucers() {
        return pojoMgr.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processProvider(ApplicationContext applicationContext, String beanName, Object bean) {
        RpcSchema rpcSchema = bean.getClass().getAnnotation(RpcSchema.class);
        if (rpcSchema == null) {
            return;
        }

        Class<?> beanCls = BeanUtils.getImplClassFromBean(bean);
        String schemaId = rpcSchema.schemaId();
        if (StringUtils.isEmpty(schemaId)) {
            Class<?>[] intfs = beanCls.getInterfaces();
            if (intfs.length == 1) {
                schemaId = intfs[0].getName();
            } else {
                throw new Error("Must be schemaId or implements only one interface");
            }
        }

        PojoProducerMeta pojoProducerMeta = new PojoProducerMeta();
        pojoProducerMeta.setSchemaId(schemaId);
        pojoProducerMeta.setInstance(bean);
        pojoProducerMeta.setInstanceClass(beanCls);

        registerPojoProducer(pojoProducerMeta);
    }
}
