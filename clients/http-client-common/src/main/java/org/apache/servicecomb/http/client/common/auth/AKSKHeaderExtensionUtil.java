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

package org.apache.servicecomb.http.client.common.auth;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

public class AKSKHeaderExtensionUtil {

  private static AKSKHeaderExtension headerExtension = new FileSystemMountAKSKHeaderExtension();

  public static Map<String, String> genAuthHeaders() {
    if (Files.exists(Paths.get(AKSKHeaderExtension.DEFAULT_SECRET_AUTH_PATH,
        AKSKHeaderExtension.DEFAULT_SECRET_AUTH_NAME))) {
      return headerExtension.getHeaders();
    }
    return Collections.emptyMap();
  }
}
