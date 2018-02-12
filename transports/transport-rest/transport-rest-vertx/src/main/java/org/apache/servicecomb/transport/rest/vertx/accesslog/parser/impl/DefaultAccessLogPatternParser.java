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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogPatternParser;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.AccessLogElementMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.AccessLogItemMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.PercentagePrefixConfigurableMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.SimpleItemMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.BytesWrittenV1Matcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.BytesWrittenV2Matcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.CookieElementMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.DatetimeConfigurableMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.DatetimeMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.DurationMillisecondMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.DurationSecondMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.FirstLineOfRequestMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.LocalHostMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.LocalPortMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.MethodMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.QueryOnlyMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.RemoteHostMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.RequestHeaderElementMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.ResponseHeaderElementMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.StatusMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.TraceIdMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.UriPathIncludeQueryMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.UriPathOnlyMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl.VersionOrProtocolMatcher;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * contains all kinds of {@link AccessLogElementMatcher},
 * generate a chain of {@link AccessLogElementExtraction} according to the raw access log pattern.
 */
public class DefaultAccessLogPatternParser implements AccessLogPatternParser {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAccessLogPatternParser.class);

  private static final List<AccessLogItemMatcher> matcherList = Arrays.asList(
      new SimpleItemMatcher(), new PercentagePrefixConfigurableMatcher()
  );

  @Override
  public List<AccessLogItemLocation> parsePattern2(String rawPattern) {
    LOGGER.info("parse access log pattern: [{}]", rawPattern);
    List<AccessLogItemLocation> locationList = new ArrayList<>();
    for (int i = 0; i < rawPattern.length(); ) {
      AccessLogItemLocation location = match(rawPattern, i);
      if (null == location) {
        break;
      }

      locationList.add(location);
      i = location.getEnd();
    }

    checkLocationList(rawPattern, locationList);

    locationList = fillInTextPlain(rawPattern, locationList);

    return locationList;
  }

  private AccessLogItemLocation match(String rawPattern, int offset) {
    AccessLogItemLocation result = null;
    for (AccessLogItemMatcher matcher : matcherList) {
      AccessLogItemLocation location = matcher.match(rawPattern, offset);
      if ((null == result) || (null != location && location.getStart() < result.getStart())) {
        // if result is null or location is nearer to offset, use location as result
        result = location;
      }
    }
    return result;
  }

  /**
   * The content not matched in rawPattern will be printed as it is, so should be converted to {@link AccessLogItemTypeEnum#TEXT_PLAIN}
   * @param rawPattern access log string pattern
   * @param locationList {@link AccessLogItemLocation} list indicating the position of each access log item
   */
  private List<AccessLogItemLocation> fillInTextPlain(String rawPattern, List<AccessLogItemLocation> locationList) {
    int cursor = 0;
    List<AccessLogItemLocation> result = new ArrayList<>();

    for (AccessLogItemLocation location : locationList) {
      if (cursor == location.getStart()) {
        result.add(location);
      } else if (cursor < location.getStart()) {
        result.add(new AccessLogItemLocation().setStart(cursor).setEnd(location.getStart()).setPlaceHolder(
            AccessLogItemTypeEnum.TEXT_PLAIN));
        result.add(location);
      }
      cursor = location.getEnd();
    }

    if (cursor < rawPattern.length()) {
      result.add(new AccessLogItemLocation().setStart(cursor).setEnd(rawPattern.length())
          .setPlaceHolder(AccessLogItemTypeEnum.TEXT_PLAIN));
    }

    return result;
  }

  private void checkLocationList(String rawPattern, List<AccessLogItemLocation> locationList) {
    int preEnd = -1;
    for (AccessLogItemLocation location : locationList) {
      if (preEnd > location.getStart()) {
        throw new IllegalArgumentException("access log pattern contains illegal placeholder, please check it.");
      }

      preEnd = location.getEnd();
    }

    if (preEnd > rawPattern.length()) {
      throw new IllegalArgumentException("access log pattern contains illegal placeholder, please check it.");
    }
  }


  private static final List<AccessLogElementMatcher> MATCHER_LIST = Arrays.asList(
      new RequestHeaderElementMatcher(),
      new DatetimeConfigurableMatcher(),
      new CookieElementMatcher(),
      new ResponseHeaderElementMatcher(),
      new DurationSecondMatcher(),
      new VersionOrProtocolMatcher(),
      new BytesWrittenV1Matcher(),
      new BytesWrittenV2Matcher(),
      new DurationMillisecondMatcher(),
      new LocalPortMatcher(),
      new LocalHostMatcher(),
      new UriPathIncludeQueryMatcher(),
      new FirstLineOfRequestMatcher(),
      new DatetimeMatcher(),
      new RemoteHostMatcher(),
      new MethodMatcher(),
      new QueryOnlyMatcher(),
      new UriPathOnlyMatcher(),
      new StatusMatcher(),
      new TraceIdMatcher());

  public static final Comparator<AccessLogElementExtraction> ACCESS_LOG_ELEMENT_EXTRACTION_COMPARATOR = Comparator
      .comparingInt(AccessLogElementExtraction::getStart);

  @Override
  public List<AccessLogElementExtraction> parsePattern(String rawPattern) {
    List<AccessLogElementExtraction> extractionList = new ArrayList<>();
    for (AccessLogElementMatcher matcher : MATCHER_LIST) {
      List<AccessLogElementExtraction> extractions = matcher.extractElementPlaceholder(rawPattern);
      if (null != extractions) {
        extractionList.addAll(extractions);
      }
    }

    extractionList.sort(ACCESS_LOG_ELEMENT_EXTRACTION_COMPARATOR);
    checkExtractionList(extractionList);
    fillInPlainTextElement(rawPattern, extractionList);

    return extractionList;
  }

  private void checkExtractionList(List<AccessLogElementExtraction> extractionList) {
    int preEnd = -1;
    for (AccessLogElementExtraction extraction : extractionList) {
      if (preEnd > extraction.getStart()) {
        throw new IllegalArgumentException("access log pattern contains illegal placeholder, please check it.");
      }

      preEnd = extraction.getEnd();
    }
  }

  /**
   * The content not matched in rawPattern will be printed as it is, so should be converted to {@link PlainTextElement}
   * @param rawPattern access log string pattern
   * @param extractionList {@link AccessLogElementExtraction} list indicating the position of each access log element
   */
  private void fillInPlainTextElement(String rawPattern, List<AccessLogElementExtraction> extractionList) {
    int cursor = 0;
    List<AccessLogElementExtraction> plainTextExtractionList = new ArrayList<>();
    for (AccessLogElementExtraction extraction : extractionList) {
      if (cursor < extraction.getStart()) {
        plainTextExtractionList.add(
            new AccessLogElementExtraction()
                .setStart(cursor)
                .setEnd(extraction.getStart())
                .setAccessLogElement(
                    new PlainTextElement(rawPattern.substring(cursor, extraction.getStart()))));
      }
      cursor = extraction.getEnd();
    }

    extractionList.addAll(plainTextExtractionList);
    extractionList.sort(ACCESS_LOG_ELEMENT_EXTRACTION_COMPARATOR);
  }
}
