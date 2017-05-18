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

package com.huawei.paas.foundation.vertx;

import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;

/**
 * AsyncResultCallback
 * @author  
 *
 * @param <T>  T
 */
public interface AsyncResultCallback<T> extends AsyncResultHandler<T> {
    /**
     * on success
     * @param data data
     */
    default void success(T data) {
        handle(Future.succeededFuture(data));
    }

    /**
     * on fail
     * @param e  err
     */
    default void fail(Throwable e) {
        handle(Future.failedFuture(e));
    }
}
