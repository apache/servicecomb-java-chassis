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

package com.huawei.paas.cse.provider.pojo.schema;

import javax.inject.Inject;

import org.springframework.beans.factory.InitializingBean;

import io.servicecomb.core.provider.producer.ProducerMeta;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年12月26日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class PojoProducerMeta extends ProducerMeta implements InitializingBean {
    @Inject
    protected PojoProducers pojoProducers;

    private String implementation;

    /**
     * 获取implementation的值
     * @return 返回 implementation
     */
    public String getImplementation() {
        return implementation;
    }

    /**
    * 对implementation进行赋值
    * @param implementation implementation的新值
    */
    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        pojoProducers.registerPojoProducer(this);
    }
}
