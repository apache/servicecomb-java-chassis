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

package io.servicecomb.core.definition;

import java.util.Collection;

import com.huawei.paas.foundation.common.RegisterManager;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年12月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class CommonService<OPERATION> {
    protected String name;

    protected RegisterManager<String, OPERATION> operationMgr;

    /**
     * <构造函数> [参数说明]
     */
    public void createOperationMgr(String operationMgrName) {
        operationMgr = new RegisterManager<>(operationMgrName);
    }

    public void regOperation(String operationName, OPERATION operaton) {
        operationMgr.register(operationName, operaton);
    }

    public OPERATION findOperation(String operation) {
        return operationMgr.findValue(operation);
    }

    public OPERATION ensureFindOperation(String operation) {
        return operationMgr.ensureFindValue(operation);
    }

    public Collection<OPERATION> getOperations() {
        return operationMgr.values();
    }

    /**
     * 获取name的值
     * @return 返回 name
     */
    public String getName() {
        return name;
    }

    /**
     * 对name进行赋值
     * @param name name的新值
     */
    public void setName(String name) {
        this.name = name;
    }
}
