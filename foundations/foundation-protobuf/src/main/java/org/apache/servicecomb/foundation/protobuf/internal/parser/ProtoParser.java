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
package org.apache.servicecomb.foundation.protobuf.internal.parser;

import java.util.Collections;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.protostuff.compiler.ParserModule;
import io.protostuff.compiler.model.Proto;
import io.protostuff.compiler.parser.FileDescriptorLoader;
import io.protostuff.compiler.parser.FileReader;
import io.protostuff.compiler.parser.FileReaderFactory;
import io.protostuff.compiler.parser.ProtoContext;

/**
 * can be reused
 */
public class ProtoParser {
  private Injector injector = Guice.createInjector(new ParserModule());

  private FileReaderFactory fileReaderFactory = injector.getInstance(FileReaderFactory.class);

  private FileReader defaultReader = fileReaderFactory.create(Collections.emptyList());

  private FileDescriptorLoader loader = injector.getInstance(FileDescriptorLoader.class);

  public Proto parseFromContent(String content) {
    ProtoContext context = loader.load(new ContentFileReader(defaultReader), content);
    return context.getProto();
  }

  public Proto parse(String name) {
    ProtoContext context = loader.load(defaultReader, name);
    return context.getProto();
  }
}
