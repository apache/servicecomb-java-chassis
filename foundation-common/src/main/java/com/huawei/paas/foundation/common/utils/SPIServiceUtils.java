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

package com.huawei.paas.foundation.common.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * SPI Service utils
 * @author  
 *
 */
public final class SPIServiceUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SPIServiceUtils.class);

    private SPIServiceUtils() {

    }

    /**
     * get target service.if target services are array,only random access to a service.
     * @param serviceType service type
     * @return target service,if no service, it will return null.
     */
    public static <T> T getTargetService(Class<T> serviceType) {
        ServiceLoader<T> loader = ServiceLoader.load(serviceType);
        Iterator<T> targetServices = loader.iterator();
        while (targetServices.hasNext()) {
            T service = targetServices.next();
            LOGGER.info("get the SPI service success, the extend service is: {}", service.getClass());
            return service;
        }
        LOGGER.info("Can not get the SPI service, the interface type is: {}", serviceType.toString());
        return null;
    }

    public static <T> List<T> getAllService(Class<T> serviceType) {
        List<T> list = new ArrayList<>();
        ServiceLoader.load(serviceType).forEach(service -> {
            list.add(service);
        });
        return list;
    }

    public static <T> T getPriorityHighestService(Class<T> serviceType) {
        String methodName = "getOrder";
        Method getOrder = ReflectionUtils.findMethod(serviceType, methodName);
        if (getOrder == null) {
            throw new Error(String.format("method %s not exists in class %s", methodName, serviceType.getName()));
        }

        int order = Integer.MAX_VALUE;
        T highestService = null;
        for (T service : getAllService(serviceType)) {
            int serviceOrder = (int) ReflectionUtils.invokeMethod(getOrder, service);
            if (serviceOrder <= order) {
                order = serviceOrder;
                highestService = service;
            }
        }
        return highestService;
    }

}
