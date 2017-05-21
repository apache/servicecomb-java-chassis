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

package io.servicecomb.core.context;

import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import io.servicecomb.core.CseContext;
import com.huawei.paas.cse.swagger.invocation.SwaggerInvocationContext;

/**
 * 设置特定的Cse Context数据
 * @author   
 * @version  [版本号, 2017年2月13日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class InvocationContext extends SwaggerInvocationContext {
    private StatusType httpStatus;

    public InvocationContext(Map<String, String> context) {
        super(context);
        httpStatus = Status.OK;
    }

    public StatusType getStatus() {
        return httpStatus;
    }

    public void setStatus(StatusType status) {
        this.httpStatus = status;
    }

    public void setStatus(int statusCode) {
        httpStatus = CseContext.getInstance().getStatusMgr().getOrCreateByStatusCode(statusCode);
    }

    public void setStatus(int statusCode, String reason) {
        httpStatus = new HttpStatus(statusCode, reason);
    }
}
