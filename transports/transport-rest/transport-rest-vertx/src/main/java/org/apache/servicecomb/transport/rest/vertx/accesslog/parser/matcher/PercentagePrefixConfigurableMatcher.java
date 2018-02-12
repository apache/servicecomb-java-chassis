package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;

public class PercentagePrefixConfigurableMatcher implements AccessLogItemMatcher {

  public static final String GENERAL_PREFIX = "%{";

  private static final Map<String, AccessLogItemTypeEnum> SUFFIX_PLACEHOLDER_ENUM_MAP = new LinkedHashMap<>();

  public static final String SUFFIX_HEAD = "}";

  static {
    SUFFIX_PLACEHOLDER_ENUM_MAP.put("}t", AccessLogItemTypeEnum.DATETIME_CONFIGURABLE);
    SUFFIX_PLACEHOLDER_ENUM_MAP.put("}i", AccessLogItemTypeEnum.REQUEST_HEADER);
    SUFFIX_PLACEHOLDER_ENUM_MAP.put("}o", AccessLogItemTypeEnum.RESPONSE_HEADER);
    SUFFIX_PLACEHOLDER_ENUM_MAP.put("}C", AccessLogItemTypeEnum.COOKIE);
  }

  @Override
  public AccessLogItemLocation match(String rawPattern, int offset) {
    int begin = rawPattern.indexOf(GENERAL_PREFIX, offset);
    if (begin < 0) {
      return null;
    }

    int end = rawPattern.indexOf(SUFFIX_HEAD, begin);
    if (end < 0) {
      return null;
    }

    for (Entry<String, AccessLogItemTypeEnum> entry : SUFFIX_PLACEHOLDER_ENUM_MAP.entrySet()) {
      if (rawPattern.startsWith(entry.getKey(), end)) {
        return new AccessLogItemLocation().setStart(begin).setEnd(end + entry.getKey().length())
            .setPlaceHolder(entry.getValue());
      }
    }

    return null;
  }
}
