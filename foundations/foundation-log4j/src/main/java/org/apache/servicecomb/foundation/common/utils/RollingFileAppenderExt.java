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
import java.io.IOException;

import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

/**
 * 扩展增加2个功能：1. 设置文件权限；2. 文件删除后重建。
 */
public class RollingFileAppenderExt extends RollingFileAppender {
  private File currentFile;

  private boolean append;

  private String logPermission;

  public String getLogPermission() {
    return logPermission;
  }

  public void setLogPermission(String logPermission) {
    this.logPermission = logPermission;
  }

  @Override
  public void setFile(String file) {
    super.setFile(file);
  }

  @Override
  public void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize) throws IOException {
    this.append = append;
    currentFile = new File(fileName);
    createCurrentFile();
    super.setFile(fileName, append, bufferedIO, bufferSize);
  }

  @Override
  protected void subAppend(LoggingEvent event) {
    // create a new file when file deleted
    if (!currentFile.exists()) {
      try {
        setFile(fileName, append, bufferedIO, bufferSize);
      } catch (IOException e) {
        LogLog.error("", e);
      }
    }
    super.subAppend(event);
  }

  protected void createCurrentFile() throws IOException {
    if (!currentFile.exists()) {
      File parent = currentFile.getParentFile();
      if (parent != null && !parent.exists()) {
        parent.mkdirs();
      }
      currentFile.createNewFile();
    }
    FilePerm.setFilePerm(currentFile, logPermission);
  }
}
