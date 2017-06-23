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
package io.servicecomb.samples.pojo.client;

import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;
import io.servicecomb.provider.pojo.RpcReference;
import io.servicecomb.samples.pojo.Compute;
import io.servicecomb.samples.pojo.Hello;
import io.servicecomb.samples.pojo.models.Person;
import org.springframework.stereotype.Component;

@Component
public class PojoClient {

    @RpcReference(microserviceName = "hello", schemaId = "hello")
    private static Hello hello;


    @RpcReference(microserviceName = "hello", schemaId = "codeFirstCompute")
    public static Compute compute;

    public static void main(String[] args) throws Exception {
        init(args);
        System.out.println(hello.sayHi("Java Chassis"));
        Person person = new Person();
        person.setName("ServiceComb/Java Chassis");
        System.out.println(hello.sayHello(person));
        System.out.println("a: 1, b=2, result=" + compute.add(1, 2));
    }

    public static void init(String[] args) throws Exception {
        if (args.length != 0){
            String fileName = args[0];
            if (!fileName.isEmpty()) {
                System.setProperty("cse.configurationSource.defaultFileName", fileName);
            }
        }
        Log4jUtils.init();
        BeanUtils.init();
        System.clearProperty("cse.configurationSource.defaultFileName");
    }
}
