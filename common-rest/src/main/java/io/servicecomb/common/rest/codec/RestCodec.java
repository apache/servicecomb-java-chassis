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

package io.servicecomb.common.rest.codec;

import java.util.List;

import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.definition.RestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.core.exception.ExceptionFactory;
import io.servicecomb.core.exception.InvocationException;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年1月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public final class RestCodec {
    private static final Logger LOG = LoggerFactory.getLogger(RestCodec.class);

    private RestCodec() {
    }

    /**
     * 将标准参数序列化成RESTful数据
     * @param args
     * @param restOperation
     * @return
     * @throws Exception
     */
    public static void argsToRest(Object[] args, RestOperationMeta restOperation,
            RestClientRequest clientRequest) throws Exception {
        int paramSize = restOperation.getParamList().size();
        if (paramSize == 0) {
            return;
        }

        if (paramSize != args.length) {
            throw new Exception("wrong number of arguments");
        }

        for (int idx = 0; idx < paramSize; idx++) {
            RestParam param = restOperation.getParamList().get(idx);
            param.getParamProcessor().setValue(clientRequest, args[idx]);
        }
    }

    /**
     * 将RESTful数据反序列化成标准pojo参数
     * @param request
     * @param restOperation
     * @return
     * @throws Exception
     */
    public static Object[] restToArgs(RestServerRequest request,
            RestOperationMeta restOperation) throws InvocationException {
        List<RestParam> paramList = restOperation.getParamList();

        try {
            Object[] paramValues = new Object[paramList.size()];
            for (int idx = 0; idx < paramList.size(); idx++) {
                RestParam param = paramList.get(idx);
                paramValues[idx] = param.getParamProcessor().getValue(request);
            }

            return paramValues;
        } catch (Exception e) {
            LOG.error("Parameter is not valid, cause " + e.getMessage());
            throw ExceptionFactory.convertProducerException(e, "Parameter is not valid.");
        }
    }
}
