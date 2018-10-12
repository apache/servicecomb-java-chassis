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

package org.apache.servicecomb.common.rest.definition.path;

import org.apache.servicecomb.common.rest.definition.RestParam;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;
import org.apache.servicecomb.foundation.common.http.HttpUtils;

/**
 * Dynamically processing path
 */
public class PathVarParamWriter extends AbstractUrlParamWriter {
  public PathVarParamWriter(RestParam param) {
    this.param = param;
  }

  @Override
  public void write(URLPathStringBuilder builder, Object[] args) throws Exception {
    String paramValue = getParamValue(args).toString();
    String encodedPathParam = HttpUtils.encodePathParam(paramValue);
    builder.appendPath(encodedPathParam);
  }
}
