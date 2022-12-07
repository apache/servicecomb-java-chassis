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

/**
 * Handles the all constant values for fault injection.
 */
public class FaultInjectionConst {

  public static final int FAULT_INJECTION_DEFAULT_VALUE = -1;

  public static final String TYPE_DELAY = "delay";

  public static final String TYPE_ABORT = "abort";

  public static final String FALLBACK_THROWEXCEPTION = "ThrowException";

  public static final String FALLBACK_RETURNNULL = "ReturnNull";

  public static final int ERROR_CODE_MIN = 200;

  public static final int ERROR_CODE_MAX = 600;
}
