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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理path中的正则表达式
 */
public class PathRegExp {
  // 不带正则表达式的group,使用这个作为正则表达式
  private static final String DEFAULT_REG_EXP = "[^/]+?";

  private static final byte NAME_READ = 2;

  private static final byte NAME_READ_READY = 3;

  private static final byte NAME_READ_START = 1;

  private static final byte REGEXP_READ = 12;

  private static final byte REGEXP_READ_READY = 13;

  private static final byte REGEXP_READ_START = 11;

  public static final String SLASH = "/";

  // 静态字符的个数
  protected int staticCharCount;

  // 包括带正则表达式和不带正则表达式的group,总数
  protected int groupCount;

  // 带正则表达式的group数
  protected int groupWithRegExpCount = 0;

  protected final Pattern pattern;

  protected final List<String> varNames = new ArrayList<>();

  public static String ensureEndWithSlash(String path) {
    if (path.endsWith(SLASH)) {
      return path;
    }

    return path + "/";
  }

  // 调用者已经保证path不以/打头
  public PathRegExp(String path)
      throws Exception {
    // a/{id}/{name:.+}/{age}/c变成下面的表达式
    // a/([^/]+?)/(.+)/([^/]+?)/c/(.*)
    final int pathLength = path.length();
    final StringBuilder pathPattern = new StringBuilder();
    for (int i = 0; i < pathLength; i++) {
      final char c = path.charAt(i);
      switch (c) {
        case '{':
          i = processGroup(path, i, pathPattern);
          groupCount++;
          break;
        case '}':
          throw new Exception("'}' is only allowed as "
              + "end of a variable name in \"" + path + "\"");
        case ';':
          throw new Exception("matrix parameters are not allowed in \"" + path
              + "\"");
        default:
          pathPattern.append(c);
          staticCharCount++;
      }
    }
    if (pathPattern.length() > 0
        && pathPattern.charAt(pathPattern.length() - 1) != '/') {
      pathPattern.append('/');
    }
    pathPattern.append("(.*)");

    pattern = Pattern.compile(pathPattern.toString());
  }

  protected int processGroup(final String path, final int braceIndex,
      final StringBuilder pathPattern) throws Exception {
    pathPattern.append('(');
    final int pathLength = path.length();
    final StringBuilder varName = new StringBuilder();
    final StringBuilder regExp = new StringBuilder();
    int state = NAME_READ_START;
    for (int i = braceIndex + 1; i < pathLength; i++) {
      final char c = path.charAt(i);
      switch (c) {
        case '{':
          throw new Exception("A variable must not contain an extra '{' in \""
              + path + "\"");
        case ' ':
        case '\t':
          state = processLineBreak(state);
          break;
        case ':':
          state = processColon(path, braceIndex, state);
          break;
        case '}':
          processBrace(path, pathPattern, varName, regExp, state);
          return i;
        default:
          state = processDefault(path, varName, regExp, state, i, c);
          break;
      }
    }
    throw new Exception("No '}' found after '{' " + "at position " + braceIndex
        + " of \"" + path + "\"");
  }

  private int processDefault(final String path, final StringBuilder varName,
      final StringBuilder regExp, int state, int i, final char c) throws Exception {
    if (state == NAME_READ_START) {
      state = NAME_READ;
      varName.append(c);
    } else if (state == NAME_READ) {
      varName.append(c);
    } else if (state == REGEXP_READ_START) {
      state = REGEXP_READ;
      regExp.append(c);
    } else if (state == REGEXP_READ) {
      regExp.append(c);
    } else {
      throw new Exception("Invalid character found at position " + i
          + " of \"" + path + "\"");
    }
    return state;
  }

  private void processBrace(final String path, final StringBuilder pathPattern,
      final StringBuilder varName, final StringBuilder regExp, int state) throws Exception {
    if (state == NAME_READ_START) {
      throw new Exception(
          "The template variable name '{}' is not allowed in "
              + "\"" + path + "\"");
    }
    if ((state == REGEXP_READ) || (state == REGEXP_READ_READY)) {
      pathPattern.append(regExp);
      if (!regExp.toString().equals(DEFAULT_REG_EXP)) {
        groupWithRegExpCount++;
      }
    } else {
      pathPattern.append(DEFAULT_REG_EXP);
    }
    pathPattern.append(')');
    this.varNames.add(varName.toString());
  }

  private int processColon(final String path, final int braceIndex, int state) throws Exception {
    if (state == NAME_READ_START) {
      throw new Exception(
          "The variable name at position must not be null at "
              + braceIndex + " of \"" + path + "\"");
    }
    if (state == NAME_READ || state == NAME_READ_READY) {
      state = REGEXP_READ_START;
    }
    return state;
  }

  private int processLineBreak(int state) {
    if (state == NAME_READ) {
      state = NAME_READ_READY;
    } else if (state == REGEXP_READ) {
      state = REGEXP_READ_READY;
    }
    return state;
  }

  // 已知/customers/{id}/address/{id}
  // @PathParam("id") String addressId
  // url:/customers/123/address/456
  // 则addressId取值为456
  // 即后面的总是覆盖前面的
  public String match(String path, Map<String, String> varValues) {
    Matcher matcher = pattern.matcher(path);
    if (!matcher.matches()) {
      return null;
    }

    for (int i = 1; i < matcher.groupCount(); i++) {
      varValues.put(varNames.get(i - 1), matcher.group(i));
    }

    return matcher.group(matcher.groupCount());
  }

  @Override
  public String toString() {
    return this.pattern.pattern();
  }

  public boolean isStaticPath() {
    return groupCount == 0;
  }

  public int getStaticCharCount() {
    return staticCharCount;
  }

  public int getGroupCount() {
    return groupCount;
  }

  public int getGroupWithRegExpCount() {
    return groupWithRegExpCount;
  }
}
