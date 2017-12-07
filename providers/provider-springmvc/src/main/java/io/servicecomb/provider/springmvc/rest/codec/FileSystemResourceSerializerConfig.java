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

package io.servicecomb.provider.springmvc.rest.codec;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.fasterxml.jackson.databind.module.SimpleModule;

import io.servicecomb.common.rest.codec.RestObjectMapper;

@Configuration
class FileSystemResourceSerializerConfig {
  @PostConstruct
  void init() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(FileSystemResource.class, new FileSystemResourceSerializer());
    RestObjectMapper.INSTANCE.registerModule(module);
  }
}
