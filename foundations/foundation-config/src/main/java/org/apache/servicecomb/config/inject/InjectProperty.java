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
package org.apache.servicecomb.config.inject;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface InjectProperty {
  /**
   * override prefix of {@link InjectProperties}
   * @return prefix of keys
   */
  String prefix() default "";

  /**
   * <pre>
   * The name of the property
   * 1.support priority
   *   high,middle,low
   * 2.support placeholder
   *   name is root.${placeholder-1}.name
   *   placeholder-1 is k1
   *   then final name is root.k1.name
   * 3.placeholder can be a list
   *   name is root.${placeholder-1}.name
   *   placeholder-1 is a list, value is k1-1/k1-2
   *   then final name is root.k1-1.name/root.k1-2.name
   * </pre>
   * @return the name of the property
   */
  String[] keys() default {};

  String defaultValue() default "";
}
