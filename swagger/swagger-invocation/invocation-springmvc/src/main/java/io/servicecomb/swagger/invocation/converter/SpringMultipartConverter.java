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

package io.servicecomb.swagger.invocation.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
class SpringMultipartConverter implements CustomizedConverter {
  @Override
  public Type getSrcType() {
    return HttpServletRequest.class;
  }

  @Override
  public Type getTargetType() {
    return MultipartFile.class;
  }

  @Override
  public Object convert(Object value) {
    Part part = (Part) value;

    return new MultipartFile() {
      @Override
      public String getName() {
        return part.getName();
      }

      @Override
      public String getOriginalFilename() {
        return part.getSubmittedFileName();
      }

      @Override
      public String getContentType() {
        return part.getContentType();
      }

      @Override
      public boolean isEmpty() {
        return part.getSize() == 0;
      }

      @Override
      public long getSize() {
        return part.getSize();
      }

      @Override
      public byte[] getBytes() throws IOException {
        return IOUtils.toByteArray(part.getInputStream());
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return part.getInputStream();
      }

      @Override
      public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileOutputStream output = new FileOutputStream(dest)) {
          IOUtils.copy(part.getInputStream(), output);
        }
      }
    };
  }
}
