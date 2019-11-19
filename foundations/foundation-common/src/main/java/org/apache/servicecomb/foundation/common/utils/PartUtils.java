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
package org.apache.servicecomb.foundation.common.utils;

import java.io.File;
import java.io.InputStream;

import javax.servlet.http.Part;

import org.apache.servicecomb.foundation.common.part.FilePart;
import org.apache.servicecomb.foundation.common.part.InputStreamPart;
import org.apache.servicecomb.foundation.common.part.ResourcePart;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public final class PartUtils {
  private PartUtils() {
  }

  public static Part getSinglePart(String name, Object value) {
    if (value == null) {
      return null;
    }

    if (value instanceof Part) {
      return (Part) value;
    }

    if (value instanceof InputStream) {
      return new InputStreamPart(name, (InputStream) value);
    }

    if (value instanceof Resource) {
      return new ResourcePart(name, (Resource) value);
    }

    if (value instanceof File) {
      return new FilePart(name, (File) value);
    }

    if (value instanceof byte[]) {
      return new ResourcePart(name, new ByteArrayResource((byte[]) value));
    }

    throw new IllegalStateException(
        String.format("File input parameter of %s could be %s / %s / %s / byte[] or %s, but got %s.",
            name,
            Part.class.getName(),
            InputStream.class.getName(),
            Resource.class.getName(),
            File.class.getName(),
            value.getClass().getName()));
  }
}
