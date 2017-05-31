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

package io.servicecomb.foundation.common.utils;

import org.springframework.aop.TargetClassAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @version  [版本号, 2016年11月22日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public final class BeanUtils {
    public static final String DEFAULT_BEAN_RESOURCE = "classpath*:META-INF/spring/*.bean.xml";

    private static ApplicationContext context;

    private BeanUtils() {
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     */
    public static void init() {
        init(DEFAULT_BEAN_RESOURCE);
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param configLocations configLocations
     */
    public static void init(String... configLocations) {
        context = new ClassPathXmlApplicationContext(configLocations);
    }

    public static ApplicationContext getContext() {
        return context;
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param applicationContext
     */
    public static void setContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    /**
     * 不应该在业务流程中频繁调用，因为内部必然会加一个锁做互斥，会影响并发度
     * @param name name
     * @param <T> T
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) context.getBean(name);
    }

    public static Class<?> getImplClassFromBean(Object bean) {
        if (TargetClassAware.class.isInstance(bean)) {
            return ((TargetClassAware) bean).getTargetClass();
        }

        return bean.getClass();
    }
}
