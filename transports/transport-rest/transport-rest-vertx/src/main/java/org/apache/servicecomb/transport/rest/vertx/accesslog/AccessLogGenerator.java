package org.apache.servicecomb.transport.rest.vertx.accesslog;

import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItemFactory;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogPatternParser;

/**
 * Accept {@link AccessLogParam} and generate access log.
 * <br/>
 * Each AccessLogParam for a line of access log.
 */
public class AccessLogGenerator {
  /**
   * traversal this array to generate access log segment.
   */
  private AccessLogElement[] accessLogElements;

  private AccessLogItemFactory accessLogItemFactory = new AccessLogItemFactory();

  public AccessLogGenerator(String rawPattern, AccessLogPatternParser accessLogPatternParser) {
    List<AccessLogItemLocation> locationList = accessLogPatternParser.parsePattern(rawPattern);

    List<AccessLogElement> itemList = accessLogItemFactory.createAccessLogItem(rawPattern, locationList);
    accessLogElements = new AccessLogElement[itemList.size()];
    accessLogElements = itemList.toArray(accessLogElements);
  }

  public String generateLog(AccessLogParam accessLogParam) {
    StringBuilder log = new StringBuilder(128);
    accessLogParam.setEndMillisecond(System.currentTimeMillis());

    AccessLogElement[] accessLogElements = getAccessLogElements();
    for (int i = 0; i < accessLogElements.length; ++i) {
      log.append(accessLogElements[i].getFormattedElement(accessLogParam));
    }

    return log.toString();
  }


  private AccessLogElement[] getAccessLogElements() {
    return accessLogElements;
  }
}
