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

package io.servicecomb.swagger.generator.springmvc;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 *
 * @version  [版本号, 2017年3月27日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@RequestMapping(
        path = "Echo",
        method = {RequestMethod.PUT},
        consumes = {"a", "b"},
        produces = {"a", "b"})
public class Echo {
    public ResponseEntity<List<User>> testResponseEntity() {
        return null;
    }

    public void emptyPath() {

    }

    @RequestMapping(
            path = "echo/{targetName}",
            method = {RequestMethod.POST},
            consumes = {"text/plain", "application/*"},
            produces = {"text/plain", "application/*"})
    public String echo(@RequestBody User srcUser, @RequestHeader String header, @PathVariable String targetName,
            @RequestParam(name = "word") String word, @RequestAttribute String form) {
        return String.format("%s %s %s %s %s", srcUser.name, header, targetName, word, form);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public void multiHttpMethod() {
    }

    @RequestMapping(path = "query")
    public void defaultParam(int query) {
    }

    @RequestMapping(path = {"a", "b"})
    public void multiPath(int query) {
    }

    public void inheritHttpMethod(int query) {
    }
}
