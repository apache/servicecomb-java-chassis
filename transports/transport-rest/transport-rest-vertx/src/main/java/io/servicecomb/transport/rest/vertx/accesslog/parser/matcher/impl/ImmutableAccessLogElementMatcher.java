package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import java.util.List;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.AccessLogElementMatcher;

/**
 * There are log elements that have no configuration parameter, so these elements can be immutable (and singleton).
 * <br/>
 * Therefore, the matching algorithm implementation of these elements can be extracted into a universal method.
 */
public abstract class ImmutableAccessLogElementMatcher implements AccessLogElementMatcher {

  protected void matchElementPlaceholder(String rawPattern, String pattern,
      List<AccessLogElementExtraction> extractionList) {
    int start = -1;
    int cursor = 0;
    while (true) {
      start = rawPattern.indexOf(pattern, cursor);

      if (start < 0) {
        break;
      } else {
        AccessLogElementExtraction extraction = new AccessLogElementExtraction(start, start + pattern.length(),
            getAccessLogElement());
        extractionList.add(extraction);
      }

      cursor = start + pattern.length();
    }
  }

  protected abstract AccessLogElement getAccessLogElement();
}
