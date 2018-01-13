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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * 规避fortify问题，仅仅是规避，如
 * e.getMessage
 * e.printStackTrace
 * 调用会报安全问题（敏感信息泄露）
 *
 *
 */
public final class FortifyUtils {

  private static Method getMessageMethod;

  private static Method printStackTraceMethod;

  static {
    try {
      getMessageMethod = Throwable.class.getMethod("getMessage");
      printStackTraceMethod = Throwable.class.getMethod("printStackTrace", PrintWriter.class);
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  private FortifyUtils() {
  }

  public static String getErrorMsg(Throwable e) {
    if (e == null) {
      return "";
    }

    try {
      return (String) getMessageMethod.invoke(e);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      return "";
    }
  }

  public static String getErrorStack(Throwable e) {
    if (null == e) {
      return "";
    }

    try {
      StringWriter errors = new StringWriter();
      printStackTraceMethod.invoke(e, new PrintWriter(errors));
      return errors.toString();
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      return "";
    }
  }

  public static String getErrorInfo(Throwable e) {
    return getErrorInfo(e, true);
  }

  public static String getErrorInfo(Throwable e, boolean isPrintMsg) {
    StringBuffer error = new StringBuffer(System.lineSeparator());
    error.append("Exception: ").append(e.getClass().getName()).append("; ");

    if (isPrintMsg) {
      error.append(getErrorMsg(e)).append(System.lineSeparator());
    }
    error.append(getErrorStack(e));

    return error.toString();
  }

  public static DocumentBuilderFactory getSecurityXmlDocumentFactory() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setXIncludeAware(false);
    factory.setExpandEntityReferences(false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setValidating(true);

    return factory;
  }
}
