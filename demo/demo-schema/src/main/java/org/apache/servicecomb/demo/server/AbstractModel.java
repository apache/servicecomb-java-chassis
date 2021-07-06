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

package org.apache.servicecomb.demo.server;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

// 当使用 List<AbstractModel> 等 Collection 类型的时候， jackson 序列化不会携带类以外的其他属性
// 即会忽略掉类型信息。 当抽象类型用于 Collection 类型参数的时候，必须使用已有的属性（例子中的 property = "type"）来确定类型，
// 并在子类中显示指定属性的值，而不能使用和依赖jackson根据类型信息自己生成的类型。

// 还有一种方法，是序列化的时候指定类型， see: https://www.studytrails.com/2016/09/12/java-jackson-serialization-list/
// 但是多数序列化接口，包括 RestTemplate 等，都无法得知参数的类型。 这种方式难于应用于开发框架实现，因此对于Collection 类型参数场景，使用已有属性
// 是最简洁的方法。

@JsonTypeInfo(
    use = Id.NAME, property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DefaultAbstractModel.class, name = "default"),
    @JsonSubTypes.Type(value = SecondAbstractModel.class, name = "second"),
})
public abstract class AbstractModel {
  protected String type;

  protected String name;

  public abstract String getType();

  public abstract String getName();

  public abstract void setName(String name);
}
