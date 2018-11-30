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
package com.fasterxml.jackson.core.base;

import org.apache.servicecomb.foundation.common.utils.JvmUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.ByteSourceJsonBootstrapper;
import com.fasterxml.jackson.core.json.ReaderBasedJsonParser;
import com.fasterxml.jackson.core.json.UTF8StreamJsonParser;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.netflix.config.DynamicPropertyFactory;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * will be deleted after jackson fix the DoS problem:
 * https://github.com/FasterXML/jackson-databind/issues/2157
 */
public class DoSFix {
  private static final String SUFFIX = "Fixed";

  private static boolean enabled = DynamicPropertyFactory.getInstance()
      .getBooleanProperty("servicecomb.jackson.fix.DoS.enabled", true).get();

  private static boolean fixed;

  private static Class<?> mappingJsonFactoryClass;

  public static synchronized void init() {
    if (fixed || !enabled) {
      return;
    }

    fix();
  }

  public static JsonFactory createJsonFactory() {
    try {
      return (JsonFactory) mappingJsonFactoryClass.newInstance();
    } catch (Throwable e) {
      throw new IllegalStateException("Failed to create JsonFactory.", e);
    }
  }

  private static void fix() {
    try {
      ClassLoader classLoader = JvmUtils.correctClassLoader(DoSFix.class.getClassLoader());
      ClassPool pool = new ClassPool(ClassPool.getDefault());
      pool.appendClassPath(new LoaderClassPath(classLoader));

      fixParserBase(classLoader, pool);
      fixReaderParser(classLoader, pool);
      fixStreamParser(classLoader, pool);
      fixByteSourceJsonBootstrapper(classLoader, pool);

      CtClass ctJsonFactoryFixedClass = fixJsonFactory(classLoader, pool);
      fixMappingJsonFactoryClass(classLoader, pool, ctJsonFactoryFixedClass);

      fixed = true;
    } catch (Throwable e) {
      throw new IllegalStateException(
          "Failed to fix jackson DoS bug.",
          e);
    }
  }

  private static void fixMappingJsonFactoryClass(ClassLoader classLoader, ClassPool pool,
      CtClass ctJsonFactoryFixedClass) throws NotFoundException, CannotCompileException {
    CtClass ctMappingJsonFactoryClass = pool
        .getAndRename(MappingJsonFactory.class.getName(), MappingJsonFactory.class.getName() + SUFFIX);
    ctMappingJsonFactoryClass.setSuperclass(ctJsonFactoryFixedClass);
    mappingJsonFactoryClass = ctMappingJsonFactoryClass.toClass(classLoader, null);
  }

  private static CtClass fixJsonFactory(ClassLoader classLoader, ClassPool pool)
      throws NotFoundException, CannotCompileException {
    CtClass ctJsonFactoryClass = pool.getCtClass(JsonFactory.class.getName());
    CtClass ctJsonFactoryFixedClass = pool.makeClass(JsonFactory.class.getName() + SUFFIX);
    ctJsonFactoryFixedClass.setSuperclass(ctJsonFactoryClass);
    for (CtMethod ctMethod : ctJsonFactoryClass.getDeclaredMethods()) {
      if (ctMethod.getName().equals("_createParser")) {
        ctJsonFactoryFixedClass.addMethod(new CtMethod(ctMethod, ctJsonFactoryFixedClass, null));
      }
    }
    ctJsonFactoryFixedClass
        .replaceClassName(ReaderBasedJsonParser.class.getName(), ReaderBasedJsonParser.class.getName() + SUFFIX);
    ctJsonFactoryFixedClass
        .replaceClassName(UTF8StreamJsonParser.class.getName(), UTF8StreamJsonParser.class.getName() + SUFFIX);
    ctJsonFactoryFixedClass.replaceClassName(ByteSourceJsonBootstrapper.class.getName(),
        ByteSourceJsonBootstrapper.class.getName() + SUFFIX);
    ctJsonFactoryFixedClass.toClass(classLoader, null);

    return ctJsonFactoryFixedClass;
  }

  private static void fixByteSourceJsonBootstrapper(ClassLoader classLoader, ClassPool pool)
      throws NotFoundException, CannotCompileException {
    CtClass ctByteSourceJsonBootstrapper = pool
        .getAndRename(ByteSourceJsonBootstrapper.class.getName(), ByteSourceJsonBootstrapper.class.getName() + SUFFIX);
    ctByteSourceJsonBootstrapper
        .replaceClassName(UTF8StreamJsonParser.class.getName(), UTF8StreamJsonParser.class.getName() + SUFFIX);
    ctByteSourceJsonBootstrapper
        .replaceClassName(ReaderBasedJsonParser.class.getName(), ReaderBasedJsonParser.class.getName() + SUFFIX);
    ctByteSourceJsonBootstrapper.toClass(classLoader, null);
  }

  private static void fixStreamParser(ClassLoader classLoader, ClassPool pool)
      throws NotFoundException, CannotCompileException {
    CtClass ctStreamClass = pool
        .getAndRename(UTF8StreamJsonParser.class.getName(), UTF8StreamJsonParser.class.getName() + SUFFIX);
    ctStreamClass.replaceClassName(ParserBase.class.getName(), ParserBase.class.getName() + SUFFIX);
    ctStreamClass.toClass(classLoader, null);
  }

  private static void fixReaderParser(ClassLoader classLoader, ClassPool pool)
      throws NotFoundException, CannotCompileException {
    CtClass ctReaderClass = pool
        .getAndRename(ReaderBasedJsonParser.class.getName(), ReaderBasedJsonParser.class.getName() + SUFFIX);
    ctReaderClass.replaceClassName(ParserBase.class.getName(), ParserBase.class.getName() + SUFFIX);
    ctReaderClass.toClass(classLoader, null);
  }

  private static void fixParserBase(ClassLoader classLoader, ClassPool pool)
      throws NotFoundException, CannotCompileException {
    CtMethod ctMethodFixed = pool.get(DoSParserFixed.class.getName()).getDeclaredMethod("_parseSlowInt");
    CtClass baseClass = pool.getAndRename(ParserBase.class.getName(), ParserBase.class.getName() + SUFFIX);
    baseClass.removeMethod(baseClass.getDeclaredMethod("_parseSlowInt"));
    baseClass.addMethod(new CtMethod(ctMethodFixed, baseClass, null));
    baseClass.toClass(classLoader, null);
  }
}
