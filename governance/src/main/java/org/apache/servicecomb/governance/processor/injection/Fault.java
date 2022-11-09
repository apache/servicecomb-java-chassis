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

package org.apache.servicecomb.governance.processor.injection;

import org.apache.servicecomb.governance.policy.FaultInjectionPolicy;

import io.vavr.CheckedFunction0;

public interface Fault {
  static <T> CheckedFunction0<T> decorateCheckedSupplier(Fault fault, CheckedFunction0<T> supplier) {
    return () -> {
      if (fault.injectFault()) {
        if (FaultInjectionConst.FALLBACK_THROWEXCEPTION.equals(fault.getPolicy().getFallbackType())) {
          throw new FaultInjectionException(
              FaultResponse.createFail(fault.getPolicy().getErrorCode(), AbortFault.ABORTED_ERROR_MSG));
        } else {
          return null;
        }
      }
      return supplier.apply();
    };
  }

  int getOrder();

  String getName();

  /*
   * If true is returned,the downgrade governance policy is executed.
   * Otherwise,the original request is directly executed.
   * */
  boolean injectFault();

  /*
   * If true is returned,the downgrade governance policy is executed.
   * Otherwise,the original request is directly executed.
   * */
  boolean injectFault(FaultParam faultParam);

  String getKey();

  FaultInjectionPolicy getPolicy();
}
