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

package org.apache.servicecomb.foundation.log.core.parser.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.log.core.element.LogItem;
import org.apache.servicecomb.foundation.log.core.element.impl.PlainTextItem;
import org.apache.servicecomb.foundation.log.core.parser.CompositeVertxRestLogItemMeta;
import org.apache.servicecomb.foundation.log.core.parser.LogItemMeta;
import org.apache.servicecomb.foundation.log.core.parser.LogPatternParser;
import org.apache.servicecomb.foundation.log.core.parser.VertxRestLogItemMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.ext.web.RoutingContext;

/**
 * The parser is used for rest-over-vertx transport.
 */
public class VertxRestLogPatternParser implements LogPatternParser<RoutingContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(VertxRestLogPatternParser.class);

  public static final Comparator<VertxRestLogItemMeta> accessLogItemMetaComparator = (m1, m2) -> {
    int result = m1.getOrder() - m2.getOrder();
    if (result != 0) {
      return result;
    }

    // one of m1 & m2 has suffix, but the other one doesn't have
    if (m1.getSuffix() == null ^ m2.getSuffix() == null) {
      return m1.getSuffix() == null ? 1 : -1;
    }

    if (null != m1.getSuffix()) {
      result = comparePlaceholderString(m1.getSuffix(), m2.getSuffix());
    }

    return 0 == result ?
        comparePlaceholderString(m1.getPrefix(), m2.getPrefix())
        : result;
  };

  private List<VertxRestLogItemMeta> metaList = new ArrayList<>();

  public VertxRestLogPatternParser() {
    List<VertxRestLogItemMeta> loadedMeta = loadVertxRestLogItemMeta();
    if (null == loadedMeta || loadedMeta.isEmpty()) {
      LOGGER.error("cannot load AccessLogItemMeta!");
      throw new IllegalStateException("cannot load AccessLogItemMeta!");
    }
    for (VertxRestLogItemMeta meta : loadedMeta) {
      if (CompositeVertxRestLogItemMeta.class.isAssignableFrom(meta.getClass())) {
        this.metaList.addAll(((CompositeVertxRestLogItemMeta) meta).getAccessLogItemMetas());
      } else {
        this.metaList.add(meta);
      }
    }
    sortAccessLogItemMeta(this.metaList);
  }

  private List<VertxRestLogItemMeta> loadVertxRestLogItemMeta() {
    return SPIServiceUtils.getOrLoadSortedService(VertxRestLogItemMeta.class);
  }

  /**
   * Behavior of this compare:
   * 1. comparePlaceholderString("abc","bbc") &lt; 0
   * 2. comparePlaceholderString("abc","ab") &lt; 0
   * 3. comparePlaceholderString("abc","abc") = 0
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
   * Sort all of the {@link LogItemMeta}, the meta that is in front of the others has higher priority.
   * 
   * Sort rule(priority decreased):
   * <ol>
   *   <li>compare the {@link LogItemMeta#getOrder()}</li>
   *   <li>compare the {@link LogItemMeta#getSuffix()} in lexicographic order, if one's suffix is start with
   *   the other one's suffix, this one(who's suffix is longer) has higher priority</li>
   *   <li>compare the {@link LogItemMeta#getPrefix()}, compare rule is the same as suffix.</li>
   * </ol>
   *
   * e.g. given a list of {@link LogItemMeta} like below:
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
  public static void sortAccessLogItemMeta(List<VertxRestLogItemMeta> accessLogItemMetaList) {
    accessLogItemMetaList.sort(accessLogItemMetaComparator);
  }

  /**
   * @param rawPattern The access log pattern string specified by users.
   * @return A list of {@linkplain LogItem} which actually generate the content of access log.
   */
  @Override
  public List<LogItem<RoutingContext>> parsePattern(String rawPattern) {
    LOGGER.info("parse the pattern of access log: [{}]", rawPattern);
    List<AccessLogItemLocation> locationList = matchAccessLogItem(rawPattern);
    locationList = fillInPlainTextLocation(rawPattern, locationList);

    return convertToItemList(rawPattern, locationList);
  }

  /**
   * Use the {@link #metaList} to match rawPattern.
   * Return a list of {@link AccessLogItemLocation}.
   * Plain text is ignored.
   */
  private List<AccessLogItemLocation> matchAccessLogItem(String rawPattern) {
    List<AccessLogItemLocation> locationList = new ArrayList<>();
    int cursor = 0;
    while (cursor < rawPattern.length()) {
      AccessLogItemLocation candidate = null;
      for (VertxRestLogItemMeta meta : metaList) {
        if (null != candidate && null == meta.getSuffix()) {
          // TODO:
          // if user define item("%{","}ab") and item("%{_","}abc") and the pattern is "%{_var}ab}abc"
          // currently the result is item("%{","_var","}ab"), plaintext("}abc")
          // is this acceptable?

          // We've gotten an AccessLogItem with suffix, so there is no need to match those without suffix,
          // just break this match loop
          cursor = candidate.tail;
          break;
        }
        if (rawPattern.startsWith(meta.getPrefix(), cursor)) {
          if (null == meta.getSuffix()) {
            // for simple type AccessLogItem, there is no need to try to match the next item.
            candidate = new AccessLogItemLocation(cursor, meta);
            cursor = candidate.tail;
            break;
          }
          // for configurable type, like %{...}i, more check is needed
          // e.g. "%{varName1}o ${varName2}i" should be divided into
          // ResponseHeaderItem with varName="varName1" and RequestHeaderItem with varName="varName2"
          // INSTEAD OF RequestHeaderItem with varName="varName1}o ${varName2"
          int rear = rawPattern.indexOf(meta.getSuffix(), cursor);
          if (rear < 0) {
            continue;
          }
          if (null == candidate || rear < candidate.suffixIndex) {
            candidate = new AccessLogItemLocation(cursor, rear, meta);
          }
          // There is a matched item which is in front of this item, so this item is ignored.
        }
      }

      if (candidate == null) {
        ++cursor;
        continue;
      }
      locationList.add(candidate);
    }

    return locationList;
  }

  /**
   * After processing of {@link #matchAccessLogItem(String)}, all of the placeholders of {@link LogItem} have been
   * picked out. So the rest part of rawPattern should be treated as plain text. Those parts will be located in this
   * method and wrapped as {@link PlainTextItem}.
   * @param rawPattern raw pattern string of access log
   * @param locationList locations picked out by {@link #matchAccessLogItem(String)}
   * @return all of the locations including {@link PlainTextItem}.
   */
  private List<AccessLogItemLocation> fillInPlainTextLocation(String rawPattern,
      List<AccessLogItemLocation> locationList) {
    List<AccessLogItemLocation> resultList = new ArrayList<>();
    if (locationList.isEmpty()) {
      resultList.add(createTextPlainItemLocation(0, rawPattern.length()));
      return resultList;
    }

    Iterator<AccessLogItemLocation> itemLocationIterator = locationList.iterator();
    AccessLogItemLocation previousItemLocation = itemLocationIterator.next();
    if (previousItemLocation.prefixIndex > 0) {
      resultList.add(createTextPlainItemLocation(0, previousItemLocation.prefixIndex));
    }
    resultList.add(previousItemLocation);

    while (itemLocationIterator.hasNext()) {
      AccessLogItemLocation thisItemLocation = itemLocationIterator.next();
      if (previousItemLocation.tail < thisItemLocation.prefixIndex) {
        resultList.add(createTextPlainItemLocation(previousItemLocation.tail, thisItemLocation.prefixIndex));
      }
      previousItemLocation = thisItemLocation;
      resultList.add(previousItemLocation);
    }

    if (previousItemLocation.tail < rawPattern.length()) {
      resultList.add(createTextPlainItemLocation(
          previousItemLocation.tail,
          rawPattern.length()));
    }
    return resultList;
  }

  private AccessLogItemLocation createTextPlainItemLocation(int front, int rear) {
    return new AccessLogItemLocation(front, rear);
  }

  private List<LogItem<RoutingContext>> convertToItemList(String rawPattern,
      List<AccessLogItemLocation> locationList) {
    List<LogItem<RoutingContext>> itemList = new ArrayList<>();

    for (AccessLogItemLocation accessLogItemLocation : locationList) {
      VertxRestLogItemMeta accessLogItemMeta = accessLogItemLocation.accessLogItemMeta;
      if (null == accessLogItemMeta) {
        // a PlainTextItem location
        itemList.add(new PlainTextItem(rawPattern.substring(
            accessLogItemLocation.prefixIndex, accessLogItemLocation.tail
        )));
        continue;
      }

      itemList.add(
          accessLogItemMeta.getLogItemCreator().createItem(
              getConfigString(rawPattern, accessLogItemLocation))
      );
    }

    return itemList;
  }

  private String getConfigString(String rawPattern, AccessLogItemLocation accessLogItemLocation) {
    if (null == accessLogItemLocation.getSuffix()) {
      // simple AccessLogItem
      return null;
    }

    return rawPattern.substring(
        accessLogItemLocation.prefixIndex + accessLogItemLocation.getPrefix().length(),
        accessLogItemLocation.suffixIndex);
  }

  private static class AccessLogItemLocation {
    /**
     * prefixIndex = rawPattern.indexOf(prefix)
     */
    int prefixIndex;

    /**
     * suffixIndex = rawPattern.indexOf(suffix)
     */
    int suffixIndex;

    /**
     * tail = suffixIndex + suffix.length()
     */
    int tail;

    VertxRestLogItemMeta accessLogItemMeta;

    /**
     * for {@link PlainTextItem} only
     */
    AccessLogItemLocation(int prefixIndex, int suffixIndex) {
      this.prefixIndex = prefixIndex;
      this.suffixIndex = suffixIndex;
      this.tail = suffixIndex;
    }

    /**
     * for configurable type AccessLogItem
     */
    AccessLogItemLocation(int prefixIndex, int suffixIndex, VertxRestLogItemMeta accessLogItemMeta) {
      this.prefixIndex = prefixIndex;
      this.suffixIndex = suffixIndex;
      this.tail = suffixIndex + accessLogItemMeta.getSuffix().length();
      this.accessLogItemMeta = accessLogItemMeta;
    }

    /**
     * for simple type AccessLogItem
     */
    AccessLogItemLocation(int prefixIndex, VertxRestLogItemMeta accessLogItemMeta) {
      this.prefixIndex = prefixIndex;
      this.suffixIndex = prefixIndex + accessLogItemMeta.getPrefix().length();
      this.tail = this.suffixIndex;
      this.accessLogItemMeta = accessLogItemMeta;
    }

    public String getPrefix() {
      if (null == accessLogItemMeta) {
        return null;
      }
      return accessLogItemMeta.getPrefix();
    }

    public String getSuffix() {
      if (null == accessLogItemMeta) {
        return null;
      }
      return accessLogItemMeta.getSuffix();
    }
  }
}
