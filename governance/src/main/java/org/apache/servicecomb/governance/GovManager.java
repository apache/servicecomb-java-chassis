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
package org.apache.servicecomb.governance;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.governance.handler.GovHandler;
import org.apache.servicecomb.governance.handler.HandlerType;
import org.apache.servicecomb.governance.handler.ext.ClientRecoverPolicy;
import org.apache.servicecomb.governance.handler.ext.ServerRecoverPolicy;
import org.apache.servicecomb.governance.policy.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;

@Component
public class GovManager {

  @Autowired
  Map<String, GovHandler> handlers;

  @Autowired(required = false)
  ServerRecoverPolicy<Object> serverRecoverPolicy;

  @Autowired(required = false)
  ClientRecoverPolicy<Object> clientRecoverPolicy;

  public Object processServer(List<Policy> policies, CheckedFunction0<Object> supplier) {
    DecorateCheckedSupplier<Object> ds = Decorators.ofCheckedSupplier(supplier);
    for (Policy policy : policies) {
      if (handlers.get(policy.handler()) == null ||
          handlers.get(policy.handler()).type() == HandlerType.CLIENT) {
        continue;
      }
      ds = handlers.get(policy.handler()).process(ds, policy);
    }

    Try<Object> of = Try.of(ds.decorate());
    return of
        .recover(throwable -> {
          if (serverRecoverPolicy == null) {
            throw (RuntimeException) throwable;
          } else {
            return serverRecoverPolicy.apply(throwable);
          }
        }).get();
  }

  public Object processClient(List<Policy> policies, CheckedFunction0<Object> supplier) {
    DecorateCheckedSupplier<Object> ds = Decorators.ofCheckedSupplier(supplier);
    for (Policy policy : policies) {
      if (handlers.get(policy.handler()) == null ||
          handlers.get(policy.handler()).type() == HandlerType.SERVER) {
        continue;
      }
      ds = handlers.get(policy.handler()).process(ds, policy);
    }
    Try<Object> of = Try.of(ds.decorate());
    return of
        .recover(throwable -> {
          if (clientRecoverPolicy == null) {
            throw (RuntimeException) throwable;
          } else {
            return clientRecoverPolicy.apply(throwable);
          }
        }).get();
  }
}
