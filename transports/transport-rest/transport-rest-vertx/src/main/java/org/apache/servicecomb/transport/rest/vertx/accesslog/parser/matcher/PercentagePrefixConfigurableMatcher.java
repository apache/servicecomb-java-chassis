package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;

public class PercentagePrefixConfigurableMatcher implements AccessLogItemMatcher {

  public static final String GENERAL_PREFIX = "%{";

  private static final Map<String, AccessLogItemTypeEnum> suffixPlaceholderEnumMap = new LinkedHashMap<>();

  public static final String SUFFIX_HEAD = "}";

  static {
    suffixPlaceholderEnumMap.put("}t", AccessLogItemTypeEnum.DATETIME_CONFIGURABLE);
    suffixPlaceholderEnumMap.put("}i", AccessLogItemTypeEnum.REQUEST_HEADER);
    suffixPlaceholderEnumMap.put("}o", AccessLogItemTypeEnum.RESPONSE_HEADER);
    suffixPlaceholderEnumMap.put("}C", AccessLogItemTypeEnum.COOKIE);
  }

  @Override
  public AccessLogItemLocation match(String rawPattern, int offset) {
    if (!rawPattern.startsWith(GENERAL_PREFIX, offset)) {
      return null;
    }

    int index = rawPattern.indexOf(SUFFIX_HEAD, offset);
    if (index < 0) {
      return null;
    }

    for (Entry<String, AccessLogItemTypeEnum> entry : suffixPlaceholderEnumMap.entrySet()) {
      if (rawPattern.startsWith(entry.getKey(), index)) {
        return new AccessLogItemLocation().setStart(offset).setEnd(index + entry.getKey().length())
            .setPlaceHolder(entry.getValue());
      }
    }

    return null;
  }
}
