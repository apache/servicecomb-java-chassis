/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.demo.jaxrs.client;

import java.util.Arrays;

import org.apache.servicecomb.config.inject.InjectProperties;
import org.apache.servicecomb.config.inject.InjectProperty;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.demo.TestMgr;
import org.springframework.stereotype.Component;

@Component
public class TestDynamicConfig implements BootListener {

  @InjectProperties(prefix = "jaxrstest.jaxrsclient")
  public class Configuration {
    /*
     * 方法的 prefix 属性值 "override" 会覆盖标注在类定义的 @InjectProperties
     * 注解的 prefix 属性值。
     *
     * keys属性可以为一个字符串数组，下标越小优先级越高。
     *
     * 这里会按照如下顺序的属性名称查找配置属性，直到找到已被配置的配置属性，则停止查找：
     * 1) jaxrstest.jaxrsclient.override.high
     * 2) jaxrstest.jaxrsclient.override.low
     *
     * 测试用例：
     * jaxrstest.jaxrsclient.override.high: hello high
     * jaxrstest.jaxrsclient.override.low: hello low
     * 预期：
     * hello high
     */
    @InjectProperty(prefix = "jaxrstest.jaxrsclient.override", keys = {"high", "low"})
    public String strValue;

    /**
     * keys支持通配符，并在可以在将配置属性注入的时候指定通配符的代入对象。
     *
     * 测试用例：
     * jaxrstest.jaxrsclient.k.value: 3
     * 预期：
     * 3
     */
    @InjectProperty(keys = "${key}.value")
    public int intValue;

    /**
     * 通配符的代入对象可以是一个字符串List，优先级遵循数组元素下标越小优先级越高策略。
     *
     * 测试用例：
     * jaxrstest.jaxrsclient.l1-1: 3.0
     * jaxrstest.jaxrsclient.l1-2: 2.0
     *
     * 预期：
     * 3.0
     */
    @InjectProperty(keys = "${full-list}")
    public float floatValue;

    /**
     * keys属性也支持多个通配符，优先级如下：首先通配符的优先级从左到右递减，
     * 然后如果通配符被代入List，遵循List中元素index越小优先级越高策略。
     *
     * 测试用例：
     * jaxrstest.jaxrsclient.low-1.a.high-1.b: 1
     * jaxrstest.jaxrsclient.low-1.a.high-2.b: 2
     * jaxrstest.jaxrsclient.low-2.a.high-1.b: 3
     * jaxrstest.jaxrsclient.low-2.a.high-2.b: 4
     * 预期：
     * 1
     */
    @InjectProperty(keys = "${low-list}.a.${high-list}.b")
    public long longValue;

    /**
     * 可以通过注解的defaultValue属性指定默认值。如果字段未关联任何配置属性，
     * 定义的默认值会生效，否则默认值会被覆盖。
     *
     * 测试用例：
     * 预期：
     * abc
     */
    @InjectProperty(defaultValue = "abc")
    public String strDef;
  }

  public void onAfterRegistry(BootEvent event) {
    Configuration configuration = SCBEngine.getInstance().getPriorityPropertyManager()
        .createConfigObject(Configuration.class,
            "key", "k",
            "low-list", Arrays.asList("low-1", "low-2"),
            "high-list", Arrays.asList("high-1", "high-2"),
            "full-list", Arrays.asList("l1-1", "l1-2")
        );
    TestMgr.check(configuration.strValue, "hello high");
    TestMgr.check(configuration.intValue, 3);
    TestMgr.check(configuration.floatValue, 3.0);
    TestMgr.check(configuration.longValue, 1);
    TestMgr.check(configuration.strDef, "abc");
  }
}
