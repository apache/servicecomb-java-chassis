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

package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemMeta;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogPatternParser;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.VertxRestAccessLogItemCreator;

import io.vertx.ext.web.RoutingContext;

/**
 * The parser is used for rest-over-vertx transport.
 */
public class VertxRestAccessLogPatternParser implements AccessLogPatternParser<RoutingContext> {
  public static final Comparator<AccessLogItemMetaWrapper> accessLogItemMetaWrapperComparator = (w1, w2) -> {
    AccessLogItemMeta meta1 = w1.getAccessLogItemMeta();
    AccessLogItemMeta meta2 = w2.getAccessLogItemMeta();
    int result = meta1.getOrder() - meta2.getOrder();
    if (result != 0) {
      return result;
    }

    // one of meta1 & meta2 has suffix, but the other one doesn't have
    if (meta1.getSuffix() == null ^ meta2.getSuffix() == null) {
      return meta1.getSuffix() == null ? 1 : -1;
    }

    if (null != meta1.getSuffix()) {
      result = comparePlaceholderString(meta1.getSuffix(), meta2.getSuffix());
    }

    return 0 == result ?
        comparePlaceholderString(meta1.getPrefix(), meta2.getPrefix())
        : result;
  };

  private List<VertxRestAccessLogItemCreator> creators = new ArrayList<>();

  private List<AccessLogItemMetaWrapper> accessLogItemMetaWrappers = new ArrayList<>();

  public VertxRestAccessLogPatternParser() {
    for (VertxRestAccessLogItemCreator creator : creators) {
      for (AccessLogItemMeta accessLogItemMeta : creator.getAccessLogItemMeta()) {
        accessLogItemMetaWrappers.add(new AccessLogItemMetaWrapper(accessLogItemMeta, creator));
      }
    }
    sortAccessLogItemMetaWrapper(accessLogItemMetaWrappers);
  }

  /**
   * Behavior of this compare:
   * 1. comparePlaceholderString("abc","bbc") < 0
   * 2. comparePlaceholderString("abc","ab") < 0
   * 3. comparePlaceholderString("abc","abc) = 0
   */
  public static int comparePlaceholderString(String s1, String s2) {
    int result = s1.compareTo(s2);
    if (0 == result) {
      return result;
    }

    // there are two possible cases:
    // 1. s1="ab", s2="def"
    // 2. s1="ab", s2="abc"
    // in the case1 just return the result, but int the case2 the result should be reversed
    return result < 0 ?
        (s2.startsWith(s1) ? -result : result)
        : (s1.startsWith(s2) ? -result : result);
  }

  /**
   * @param rawPattern The access log pattern string specified by users.
   * @return A list of {@linkplain AccessLogItem} which actually generate the content of access log.
   */
  @Override
  public List<AccessLogItem<RoutingContext>> parsePattern(String rawPattern) {
    List<AccessLogItem<RoutingContext>> itemList = new ArrayList<>();
    // the algorithm is unimplemented.
    return itemList;
  }

  /**
   * Sort all of the {@link AccessLogItemMetaWrapper}, the wrapper that is in front of the others has higher priority.
   * <p/>
   * Sort rule(priority decreased):
   * <ol>
   *   <li>compare the {@link AccessLogItemMeta#order}</li>
   *   <li>compare the {@link AccessLogItemMeta#suffix} in lexicographic order, if one's suffix is start with
   *   the other one's suffix, this one(who's suffix is longer) has higher priority</li>
   *   <li>compare the {@link AccessLogItemMeta#prefix}, compare rule is the same as suffix.</li>
   * </ol>
   * <p/>
   * e.g. given a list of {@link AccessLogItemMeta} like below:
   * <ol>
   * <li>(%ac{,}bcd)</li>
   * <li>(%ac{,}bc)</li>
   * <li>(%ac{,}a)</li>
   * <li>(%ac,)</li>
   * <li>(%b,)</li>
   * <li>(%a)</li>
   * <li>(%{,}b)</li>
   * <li>(%{,}bc)</li>
   * </ol>
   * the result is:
   * <ol>
   * <li>(%ac{,}a)</li>
   * <li>(%ac{,}bcd)</li>
   * <li>(%ac{,}bc)</li>
   * <li>(%{,}bc)</li>
   * <li>(%{,}b)</li>
   * <li>(%ac,)</li>
   * <li>(%a)</li>
   * <li>(%b,)</li>
   * </ol>
   */
  private void sortAccessLogItemMetaWrapper(List<AccessLogItemMetaWrapper> accessLogItemMetaWrapperList) {
    accessLogItemMetaWrapperList.sort(accessLogItemMetaWrapperComparator);
  }

  public static class AccessLogItemMetaWrapper {
    private AccessLogItemMeta accessLogItemMeta;

    private VertxRestAccessLogItemCreator vertxRestAccessLogItemCreator;

    public AccessLogItemMetaWrapper(AccessLogItemMeta accessLogItemMeta,
        VertxRestAccessLogItemCreator vertxRestAccessLogItemCreator) {
      this.accessLogItemMeta = accessLogItemMeta;
      this.vertxRestAccessLogItemCreator = vertxRestAccessLogItemCreator;
    }

    public AccessLogItemMeta getAccessLogItemMeta() {
      return accessLogItemMeta;
    }

    public VertxRestAccessLogItemCreator getVertxRestAccessLogItemCreator() {
      return vertxRestAccessLogItemCreator;
    }
  }
}
