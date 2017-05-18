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

package com.huawei.paas.foundation.common.lock;

/**
 * 配置异常
 * @author   
 * @version  [版本号, 2016年5月11日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class LockException extends RuntimeException {
    /**
     * 注释内容
     */
    private static final long serialVersionUID = -6761470821175384480L;

    /**
     * <默认构造函数>
     */
    public LockException() {
        super();
    }

    /**
     * 构造函数
     * @param msg String
     * @param throwable throwable
     */
    public LockException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    /**
     * 构造函数
     * @param msg String
     */
    public LockException(String msg) {
        super(msg);
    }
}
