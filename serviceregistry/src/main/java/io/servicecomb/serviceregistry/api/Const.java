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

package io.servicecomb.serviceregistry.api;

/**
 * Created by   on 2017/1/9.
 */
public final class Const {
    private Const() {
    }

    public static final String REGISTRY_APP_ID = "default";

    public static final String REGISTRY_SERVICE_NAME = "SERVICECENTER";

    public static final String REGISTRY_VERSION = "3.0.0";

    public static final String MS_API_PATH = "/registry/v3";

    public static final String MICROSERVICE_PATH = "/microservices";

    public static final String SCHEMA_PATH = "/schemas";

    public static final String INSTANCES_PATH = "/instances";

    public static final String HEARTBEAT_PATH = "/heartbeat";

    public static final String EXISTENCE_PATH = "/existence";

    public static final String WATCHER_PATH = "/watcher";

    public static final String APP_SERVICE_SEPARATOR = ":";

    public static final String PATH_CHECKSESSION = "checksession";

    public static final String PROPERTIES_PATH = "/properties";
}
