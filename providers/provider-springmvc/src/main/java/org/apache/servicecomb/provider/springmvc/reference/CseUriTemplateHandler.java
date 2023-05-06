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
package org.apache.servicecomb.provider.springmvc.reference;

import java.net.URI;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.springframework.web.util.DefaultUriBuilderFactory;

public class CseUriTemplateHandler extends DefaultUriBuilderFactory {
  public static final String APP_SERVICE_SEPARATOR_INTERNAL = ".";

  public CseUriTemplateHandler() {
  }

  @Override
  public URI expand(String uriTemplate, Map<String, ?> uriVars) {
    return super.expand(parseUrl(uriTemplate), uriVars);
  }

  @Override
  public URI expand(String uriTemplate, Object... uriVars) {
    return super.expand(parseUrl(uriTemplate), uriVars);
  }

  private static String parseUrl(String uriTemplate) {
    int indexSchema = -1;
    if (uriTemplate.startsWith(RestConst.URI_PREFIX)) {
      indexSchema = RestConst.URI_PREFIX.length();
    }
    if (uriTemplate.startsWith(RestConst.URI_PREFIX_NEW)) {
      indexSchema = RestConst.URI_PREFIX_NEW.length();
    }
    if (indexSchema != -1) {
      int indexPath = uriTemplate.indexOf("/", indexSchema);
      String host = uriTemplate.substring(indexSchema, indexPath);
      host = host.replace(DefinitionConst.APP_SERVICE_SEPARATOR, APP_SERVICE_SEPARATOR_INTERNAL);
      return uriTemplate.substring(0, indexSchema) + host + uriTemplate.substring(indexPath);
    }
    return uriTemplate;
  }
}
