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

package org.apache.servicecomb.foundation.common;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterManager<KEY, VALUE> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterManager.class);

  private String name;

  private String registerErrorFmt = "Not allow register repeat data, name=%s, key=%s";

  private Map<KEY, VALUE> objMap = new ConcurrentHashMap<>();

  private Object lockObj = new Object();

  public RegisterManager(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getRegisterErrorFmt() {
    return registerErrorFmt;
  }

  public void setRegisterErrorFmt(String registerErrorFmt) {
    this.registerErrorFmt = registerErrorFmt;
  }

  public void register(KEY key, VALUE value) {
    synchronized (lockObj) {
      if (objMap.get(key) != null) {
        // 不允许重复注册
        String msg = String.format(registerErrorFmt, name, key);
        LOGGER.error(msg);

        // 禁止启动
        throw new Error(msg);
      }
      objMap.put(key, value);
    }
  }

  public VALUE findValue(KEY key) {
    return objMap.get(key);
  }

  public VALUE ensureFindValue(KEY key) {
    VALUE value = objMap.get(key);
    if (value == null) {
      String msg = String.format("Can not find value, name=%s, key=%s", name, key);
      throw new Error(msg);
    }

    return value;
  }

  public Collection<KEY> keys() {
    return objMap.keySet();
  }

  public Collection<VALUE> values() {
    return objMap.values();
  }
}
